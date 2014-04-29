package org.nefilim.snsmonitor.asg

import akka.actor.{ActorRef, Props, Actor, ActorLogging}
import scala.concurrent.Future
import spray.client.pipelining._
import spray.http.{StatusCodes, HttpRequest, HttpResponse}
import org.nefilim.snsmonitor.asg.SNSMonitorAPI.SNSSubscriptionConfirm
import org.nefilim.snsmonitor.asg.SNSMonitorAPI.SNSNotification
import scala.util.{Failure, Success}
import spray.json.{DefaultJsonProtocol, JsonParser}
import org.nefilim.chefclient._
import scala.language.postfixOps

/**
 * Created by peter on 4/23/14.
 */
object MonitorService {

  case class RemoveEC2InstanceFromChef(ec2InstanceId: String, originalNotification: SNSNotification, requester: ActorRef, attempt: Int = 1)
  case class NodeRemovedFromChef(originalRequest: RemoveEC2InstanceFromChef)
  case class FailedToRemoveNodeFromChef(originalRequest: RemoveEC2InstanceFromChef)

  case class AWSAutoScalingNotification(
           StatusCode: Option[String],
           Service: String,
           AutoScalingGroupName: String,
           Description: Option[String],
           ActivityId: Option[String],
           Event: String,
           AutoScalingGroupARN: String,
           Progress: Option[Int],
           Time: String,
           AccountId: String,
           RequestId: String,
           StatusMessage: Option[String],
           EndTime: Option[String],
           EC2InstanceId: Option[String],
           StartTime: Option[String],
           Cause: Option[String])

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val AWSAutoScalingNotificationFormat = jsonFormat16(AWSAutoScalingNotification)
  }


  def props(chefClient: ChefClient) = Props(classOf[MonitorService], chefClient)
}
import MonitorService.MyJsonProtocol._
import MonitorService._

class MonitorService(chefClient: ChefClient) extends Actor with ActorLogging {

  import context.dispatcher

  def receive: Receive = {
    case s:SNSNotification =>
      log.info("processing notification {}", s)
      s.Message match {
        case msg if msg.contains(""""Service":"AWS Auto Scaling"""") =>
          processASGEvent(s)
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

  private[asg] def processASGEvent(notification: SNSNotification) {
    log.debug("processing ASG event in notification {}", notification)
    val json = JsonParser(notification.Message)
    val asgEvent = json.convertTo[AWSAutoScalingNotification]
    asgEvent.Event.toLowerCase match {
      case "autoscaling:ec2_instance_terminate" =>
        asgEvent.EC2InstanceId.map { ec2InstanceId =>
          log.info("group {} scaling down, removing node {}", asgEvent.AutoScalingGroupName, ec2InstanceId)
          context.actorOf(RemoveChefNodeCommandActor.props(chefClient)) ! RemoveEC2InstanceFromChef(ec2InstanceId, notification, sender())
        }
        if (!asgEvent.EC2InstanceId.isDefined)
          log.error("we receive a ec2_instance_terminate notification but without a ec2InstanceId?? {}", asgEvent)
      case _ =>
        log.info("ignoring other ASG event {}", asgEvent.Event)
        sender ! (StatusCodes.OK, s"Ignoring ${notification.MessageId}")
    }
  }

  lazy val pipeline: HttpRequest => Future[HttpResponse] = (
      sendReceive
    )

  private[asg] def confirmSubscription(notification: SNSSubscriptionConfirm): Future[HttpResponse] = {
    pipeline(Get(notification.SubscribeURL))
  }

}
