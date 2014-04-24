package org.nefilim.snsmonitor.asg

import akka.actor.ActorDSL._
import org.nefilim.snsmonitor.asg.SNSMonitorAPI.{SNSSubscriptionConfirm, SNSNotification}
import spray.http.StatusCodes
import spray.routing.HttpService
import akka.actor.ActorLogging

/**
 * Created by peter on 4/23/14.
 */
trait StubServiceActors extends ServiceActors with HttpService {
  val monitorService = actor(new Act with ActorLogging {
    become {
      case s:SNSSubscriptionConfirm ⇒
        log.info("sending back OK")
        sender() ! (StatusCodes.OK, "OK")
      case s:SNSNotification ⇒
        log.info("sending back OK")
        sender() ! (StatusCodes.OK, "OK")
      case _ =>
        log.warning("dropping unknown")
        sender() ! (StatusCodes.ServerError, "unknown message")
    }
  })
}
