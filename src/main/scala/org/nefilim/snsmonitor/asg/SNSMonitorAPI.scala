package org.nefilim.snsmonitor.asg

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

object SNSMonitorAPI extends Logging {
  private[asg] val ASGMonitorAPIVersion = "v1"
  private[asg] val ASGEventPath = "asgevent"

  def eventRouteBase(innerRoute: Route): Route = {
    apiRoot {
      pathPrefix(ASGMonitorAPIVersion) {
        innerRoute
      }
    }
  }

  case class SNSSubscriptionConfirm(
        Message: String,
        MessageId: String,
        Signature: String,
        SignatureVersion: String,
        SigningCertURL: String,
        SubscribeURL: String,
        Timestamp: String,
        Token: String,
        TopicArn: String,
        Type: String)

  case class SNSNotification(
          Message: String,
          MessageId: String,
          Signature: String,
          SignatureVersion: String,
          SigningCertURL: String,
          Subject: String,
          Timestamp: String,
          TopicArn: String,
          Type: String,
          UnsubscribeURL: String)

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val snsNotificationFormat = jsonFormat10(SNSNotification)
    implicit val snsSubscriptionConfirmFormat = jsonFormat10(SNSSubscriptionConfirm)
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
                val json = JsonParser(ctx.request.entity.asString(HttpCharsets.`UTF-8`))
                header.value.toLowerCase match {
                  case "subscriptionconfirmation" =>
                    val subscriptionConfirmation = json.convertTo[SNSSubscriptionConfirm]
                    logger.info("got a subscription confirmation! {}", subscriptionConfirmation)
                    subscriptionConfirmation

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

