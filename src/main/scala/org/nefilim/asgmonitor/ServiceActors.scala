package org.nefilim.asgmonitor

import akka.actor.{ActorLogging, Actor, ActorRef}
import javax.management.Notification
import scala.concurrent.ExecutionContext

/**
 * Created by peter on 4/23/14.
 */
trait ServiceActors {
  val monitorService: ActorRef
}

trait AkkaExecutionContextProvider {
  implicit val executionContext: ExecutionContext
}

class MonitorService extends Actor with ActorLogging {
  def receive: Receive = {
    case n: Notification =>
      log.info("got notification {}", n)
  }
}

