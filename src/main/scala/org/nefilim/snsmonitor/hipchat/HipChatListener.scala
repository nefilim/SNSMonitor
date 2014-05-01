package org.nefilim.snsmonitor.hipchat

import akka.actor.{Props, Actor, ActorLogging}
import scala.language.postfixOps
import org.nefilim.snsmonitor.domain.Internal.AutoScalingNotification
import org.nefilim.hipchatclient.HipChatClient


object HipChatListener {
  def props(hipChatToken: String, hipChatRoom: String) = Props(classOf[HipChatListener], hipChatToken, hipChatRoom)
}

class HipChatListener(hipChatToken: String, hipChatRoom: String) extends Actor with ActorLogging {
  log.info("registering HipChatListener")
  private val hipChatClient = HipChatClient(hipChatToken)

  def receive: Receive = {
    case n:AutoScalingNotification =>
      val message = s"<b>AutoScalingNotification</b>: group [${n.AutoScalingGroupName}] description [${n.Description.getOrElse("none")}] start time [${n.StartTime.getOrElse("none")}]"
      hipChatClient.sendRoomNotification(hipChatRoom, message)
  }

}
