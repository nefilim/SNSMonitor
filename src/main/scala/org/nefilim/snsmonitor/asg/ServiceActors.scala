package org.nefilim.snsmonitor.asg

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


