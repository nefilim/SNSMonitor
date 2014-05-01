package org.nefilim.snsmonitor.service

import akka.actor.{ActorRef, Props, Actor, ActorLogging}
import scala.util.{Failure, Success}
import org.nefilim.chefclient.ChefClient
import scala.concurrent.duration._
import scala.language.postfixOps
import org.nefilim.snsmonitor.domain.API.SNSNotification
import org.nefilim.snsmonitor.service.RemoveChefNodeCommandActor._

/**
 * Created by peter on 4/24/14.
 *
 * searches for the chef node based on the EC2 instance ID
 * deletes the chef node
 * deletes the chef client for the node
 */
object RemoveChefNodeCommandActor {
  // external
  case class RemoveEC2InstanceFromChef(ec2InstanceId: String, originalNotification: SNSNotification, requester: ActorRef, attempt: Int = 1)
  case class NodeRemovedFromChef(originalRequest: RemoveEC2InstanceFromChef)
  case class FailedToRemoveNodeFromChef(originalRequest: RemoveEC2InstanceFromChef)

  // internal
  case class RemoveChefNodeAndClient(node: String, originalRequest: RemoveEC2InstanceFromChef)
  //case class FindChefNodeForEC2InstanceId(r: RemoveEC2InstanceFromChef)

  def props(chefClient: ChefClient) = Props(classOf[RemoveChefNodeCommandActor], chefClient)
}

class RemoveChefNodeCommandActor(chefClient: ChefClient) extends Actor with ActorLogging {

  import context.dispatcher

  private var nodeDeleted: Option[Boolean] = None
  private var clientDeleted: Option[Boolean] = None
  private var requester: Option[ActorRef] = None

  def receive: Receive = {
    case r:RemoveEC2InstanceFromChef =>
      requester = Some(sender())
      log.debug("searching for node {}", r.ec2InstanceId)
      chefClient.searchNodeIndex(s"instance_id:${r.ec2InstanceId}").onComplete {
        case Success(searchResult) =>
          searchResult match {
            case Right(result) if (!result.rows.isEmpty) =>
              log.debug("found node from search")
              self ! RemoveChefNodeAndClient(result.rows.head.name, r)
            case Right(result) if (result.rows.isEmpty) =>
              log.warning("no chef node found for ec2 instance id {}, previously deleted?", r.ec2InstanceId)
              requester.map(_ ! NodeRemovedFromChef(r))
            case Left(l) =>
              log.error("something went wrong parsing search response {}", l)
              requester.map(_ ! FailedToRemoveNodeFromChef(r))
          }
        case Failure(f) =>
          log.error("failed to search chef index", f)
          if (r.attempt < 5)
            context.system.scheduler.scheduleOnce((5*r.attempt seconds), self, r.copy(attempt = r.attempt + 1))
          else {
            log.error("failed to find node in search index after 5 retries")
            requester.map(_ ! FailedToRemoveNodeFromChef(r))
          }
      }

    case RemoveChefNodeAndClient(node, originalRequest) =>
      chefClient.deleteNode(node).onComplete {
        case Success(result) =>
          result match {
            case Right(r) =>
              log.debug("deleted node {}, result {}", r, result)
              nodeDeleted = Some(true)
              if (isDone())
                requester.map(_ ! NodeRemovedFromChef(originalRequest))
            case Left(l) =>
          }
        case Failure(f) =>
          log.error(f, "failed to delete node {}", node)
          requester.map(_ ! FailedToRemoveNodeFromChef(originalRequest))
      }
      chefClient.deleteClient(node).onComplete {
        case Success(result) =>
          result match {
            case Right(r) =>
              log.debug("deleted client {}, result {}", r, result)
              clientDeleted = Some(true)
              if (isDone())
                requester.map(_ ! NodeRemovedFromChef(originalRequest))
            case Left(l) =>
          }
        case Failure(f) =>
          log.error(f, "failed to delete client {}", node)
          requester.map(_ ! FailedToRemoveNodeFromChef(originalRequest))
      }
  }

  private def isDone(): Boolean = {
    nodeDeleted.isDefined && clientDeleted.isDefined
  }

}
