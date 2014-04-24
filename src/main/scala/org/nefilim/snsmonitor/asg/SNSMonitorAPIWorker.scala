package org.nefilim.snsmonitor.asg

import akka.actor.{Props, Actor}
import spray.http.{StatusCodes, HttpResponse, HttpRequest, Timedout}

/**
 * Created by peter on 3/16/14.
 */
object SNSMonitorAPIWorker {
  def props() = Props(classOf[SNSMonitorAPIWorker])
}

class SNSMonitorAPIWorker
  extends Actor
  with SNSMonitorAPI
  with ServiceActors
  with AkkaExecutionContextProvider
{
  def actorRefFactory = context
  implicit val executionContext = context.dispatcher  // TODO we want a custom dispatcher here? don't want to block request handling

  val monitorService = context.actorOf(MonitorService.props(), "monitorServiceActor")

  def receive = handleTimeouts orElse router

  val route = eventRoute

  def router = runRoute(route)

  def handleTimeouts: Receive = {
    case Timedout(x: HttpRequest) =>
      logger.warn(s"request ${x} timed out")
      sender ! HttpResponse(StatusCodes.RequestTimeout, s"the request ${x} timed out")
  }
}

