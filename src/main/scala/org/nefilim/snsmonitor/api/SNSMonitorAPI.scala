package org.nefilim.snsmonitor.api

import spray.routing._

import SNSMonitorAPI.eventRouteBase
import spray.routing.Directives._
import com.typesafe.scalalogging.slf4j.Logging
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps
import API._
import spray.json._
import spray.http._
import spray.httpx.SprayJsonSupport
import akka.pattern._
import spray.httpx.marshalling.MetaMarshallers
import scala.concurrent.Promise
import org.nefilim.snsmonitor.domain.API._
import org.nefilim.snsmonitor.service.{AkkaExecutionContextProvider, ServiceActors}

object SNSMonitorAPI extends Logging {
  private[api] val ASGMonitorAPIVersion = "v1"
  private[api] val ASGEventPath = "event"

  def eventRouteBase(innerRoute: Route): Route = {
    apiRoot {
      pathPrefix(ASGMonitorAPIVersion) {
        innerRoute
      }
    }
  }

}

import SNSMonitorAPI._
import MyJsonProtocol._

trait SNSMonitorAPI extends HttpService with Logging with SprayJsonSupport with MetaMarshallers { this: ServiceActors with AkkaExecutionContextProvider =>

  implicit val timeout = Timeout(5 seconds)

  val eventRoute =
    eventRouteBase {
      path(ASGEventPath) {
        post {
          logRequestResponse(logRejections _) { ctx =>
            ctx.complete {
              val message = ctx.request.headers.find(_.lowercaseName == "x-amz-sns-message-type").map { header =>  // headerByValue doesn't work anymore once we extracted ctx, why??
                logger.info("raw request {}", ctx.request.entity.asString)
                val json = JsonParser(ctx.request.entity.asString(HttpCharsets.`UTF-8`))
                header.value.toLowerCase match {
                  case "subscriptionconfirmation" =>
                    val subscriptionConfirmation = json.convertTo[SNSSubscriptionConfirm]
                    logger.info("got a subscription confirmation! {}", subscriptionConfirmation)
                    subscriptionConfirmation

                  case "unsubscribeconfirmation" =>
                    logger.warn("ignoring UnsubscribeConfirmation")

                  case "notification" =>
                    val notification = json.convertTo[SNSNotification]
                    logger.info("got a notification confirmation! {}", notification)
                    notification
                }
              }
              logger.info("extracted message {}", message)
              message.fold(Promise.successful[(StatusCode, String)]((StatusCodes.InternalServerError, "Unknown message")).future)(a => (monitorService ? a).mapTo[(StatusCode, String)])
            }
          }
        }
      }
    }
}

