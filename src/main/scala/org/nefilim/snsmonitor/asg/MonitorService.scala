package org.nefilim.snsmonitor.asg

import akka.actor.{Props, Actor, ActorLogging}
import scala.concurrent.Future
import spray.client.pipelining._
import spray.http.{StatusCodes, HttpRequest, HttpResponse}
import org.nefilim.snsmonitor.asg.SNSMonitorAPI.SNSSubscriptionConfirm
import org.nefilim.snsmonitor.asg.SNSMonitorAPI.SNSNotification
import scala.util.{Failure, Success}

/**
 * Created by peter on 4/23/14.
 */
object MonitorService {
  def props() = Props(classOf[MonitorService])
}

class MonitorService extends Actor with ActorLogging {

  import context.dispatcher

  def receive: Receive = {
    case s:SNSNotification =>
      log.info("dropping {}", s)
    case s:SNSSubscriptionConfirm =>
      log.info("subscription confirmation request {}", s)
      val requester = sender()
      confirmSubscription(s).onComplete {
        case Success(response) =>
          requester ! (StatusCodes.OK, s"Subscribed to ${s.MessageId}")
        case Failure(f) =>
          log.error(f, "failed to subscribe {}", s)
          // retry for retriable problems
          requester ! (StatusCodes.BadGateway, s"Failed to subscribe to ${s.MessageId}")
      }
  }

  lazy val pipeline: HttpRequest => Future[HttpResponse] = (
      sendReceive
    )

  private[asg] def confirmSubscription(notification: SNSSubscriptionConfirm): Future[HttpResponse] = {
    pipeline(Get(notification.SubscribeURL))
  }

}
