package org.nefilim.snsmonitor.service

import akka.actor.{Props, Actor}
import spray.http.{StatusCodes, HttpResponse, HttpRequest, Timedout}
import org.nefilim.chefclient.ChefClient
import com.typesafe.config.ConfigFactory
import org.nefilim.snsmonitor.hipchat.HipChatListener
import org.nefilim.snsmonitor.domain.Internal.MonitorEvent
import org.nefilim.snsmonitor.api.{SNSMonitorAPI, HealthAPI}

/**
 * Created by peter on 3/16/14.
 */
object SNSMonitorAPIWorker {
  def props() = Props(classOf[SNSMonitorAPIWorker])
}

class SNSMonitorAPIWorker
  extends Actor
  with SNSMonitorAPI
  with HealthAPI
  with ServiceActors
  with AkkaExecutionContextProvider
{
  def actorRefFactory = context
  implicit val executionContext = context.dispatcher  // TODO we want a custom dispatcher here? don't want to block request handling

  val config = ConfigFactory.load() // application.conf

  val chefClientId = config.getString("snsMonitor.chef.clientId")
  val chefOrganization = if (!config.hasPath("snsMonitor.chef.organization")) None else Some(config.getString("snsMonitor.chef.organization"))
  val chefClientKeyPath = config.getString("snsMonitor.chef.clientKeyPath")

  val hipChatToken = if (!config.hasPath("snsMonitor.hipChat.token")) None else Some(config.getString("snsMonitor.hipChat.token"))
  val hipChatRoom = if (!config.hasPath("snsMonitor.hipChat.room")) None else Some(config.getString("snsMonitor.hipChat.room"))
  val hipChatEnabled = hipChatToken.isDefined && hipChatRoom.isDefined

  if (hipChatEnabled) {
    val listener = context.actorOf(HipChatListener.props(hipChatToken.get, hipChatRoom.get))
    context.system.eventStream.subscribe(listener, classOf[MonitorEvent])
  }

  val chefClient = ChefClient(chefClientKeyPath, chefClientId, "api.opscode.com", chefOrganization.map("/organizations/" + _))
  val monitorService = context.actorOf(MonitorService.props(chefClient), "monitorServiceActor")

  def receive = handleTimeouts orElse router

  val route = eventRoute ~ healthRoute

  def router = runRoute(route)

  def handleTimeouts: Receive = {
    case Timedout(x: HttpRequest) =>
      logger.warn(s"request ${x} timed out")
      sender ! HttpResponse(StatusCodes.RequestTimeout, s"the request ${x} timed out")
  }
}

