package org.nefilim.snsmonitor.service

import akka.actor.{Props, Actor, ActorLogging}
import scala.concurrent.Future
import spray.client.pipelining._
import spray.http.{StatusCodes, HttpRequest, HttpResponse}
import scala.util.{Failure, Success}
import spray.json.JsonParser
import org.nefilim.chefclient._
import scala.language.postfixOps
import org.nefilim.snsmonitor.domain.API._
import org.nefilim.snsmonitor.service.RemoveChefNodeCommandActor._
import org.nefilim.snsmonitor.domain.Internal.{AutoScalingNotificationType, AutoScalingNotification}
import org.nefilim.snsmonitor.domain.Internal.InternalJsonProtocol._

/**
 * Created by peter on 4/23/14.
 */
object MonitorService {
  def props(chefClient: ChefClient) = Props(classOf[MonitorService], chefClient)
}

class MonitorService(chefClient: ChefClient) extends Actor with ActorLogging {

  import context.dispatcher

  def receive: Receive = {
    case s:SNSNotification =>
      log.info("processing notification {}", s)
      s.Message match {
        case msg if msg.contains(""""Service":"AWS Auto Scaling"""") =>
          processAutoScalingEvent(s)
        case _ =>
          log.info("ignoring message {}", s.Message)
          sender ! (StatusCodes.OK, s"Ignoring ${s.MessageId}")
      }

    case s:SNSSubscriptionConfirm =>
      log.info("processing subscription confirmation request {}", s)
      val requester = sender()
      confirmSubscription(s).onComplete {
        case Success(response) =>
          log.info("successfully subscribed to {}: {}", s.MessageId, response)
          requester ! (StatusCodes.OK, s"Subscribed to ${s.MessageId}")
        case Failure(f) =>
          log.error(f, "failed to subscribe {}", s)
          // TODO retry for retriable problems
          requester ! (StatusCodes.BadGateway, s"Failed to subscribe to ${s.MessageId}")
      }

    case NodeRemovedFromChef(r) =>
      log.info("done processing notification, node was removed {}", r)
      context.stop(sender)
      r.requester ! (StatusCodes.OK, "node removed")

    case FailedToRemoveNodeFromChef(r) =>
      log.error("failed to remove node {}", r)
      // retriable?
      context.stop(sender)
      r.requester ! (StatusCodes.InternalServerError, "failed to remove node") // push back to SNS to retry
  }

  private[service] def processAutoScalingEvent(notification: SNSNotification) {
    log.debug("processing ASG event in notification {}", notification)
    val json = JsonParser(notification.Message)
    val autoScalingNotification = json.convertTo[AutoScalingNotification]
    context.system.eventStream.publish(autoScalingNotification) // integration point for best effort/notification services
    AutoScalingNotificationType.fromString(autoScalingNotification.Event) match {
      case Some(AutoScalingNotificationType.EC2InstanceTerminate) =>
        autoScalingNotification.EC2InstanceId.map { ec2InstanceId =>
          log.info("group {} scaling down, removing node {}", autoScalingNotification.AutoScalingGroupName, ec2InstanceId)
          context.actorOf(RemoveChefNodeCommandActor.props(chefClient)) ! RemoveEC2InstanceFromChef(ec2InstanceId, notification, sender())
        }
        if (!autoScalingNotification.EC2InstanceId.isDefined)
          log.error("we receive a ec2_instance_terminate notification but without a ec2InstanceId?? {}", autoScalingNotification)

      case None =>
        log.error("encountered unknown autoscaling type [{}], dropping", autoScalingNotification.Event)
        sender ! (StatusCodes.OK, s"Ignoring ${notification.MessageId}")

      case _ =>
        log.info("ignoring other ASG event {}", autoScalingNotification.Event)
        sender ! (StatusCodes.OK, s"Ignoring ${notification.MessageId}")
    }
  }

  lazy val pipeline: HttpRequest => Future[HttpResponse] = (
      sendReceive
    )

  private[service] def confirmSubscription(notification: SNSSubscriptionConfirm): Future[HttpResponse] = {
    pipeline(Get(notification.SubscribeURL))
  }

}
