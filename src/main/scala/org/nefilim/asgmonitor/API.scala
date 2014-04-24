package org.nefilim.asgmonitor

import spray.routing._
import spray.routing.Directives._
import spray.http.HttpRequest
import spray.routing.directives.LogEntry
import scala.Some
import akka.event.Logging

object API {
  val ApiRootOU = "asgm"

  def apiRoot(innerRoute: Route) = {
    pathPrefix(ApiRootOU) {
      innerRoute
    }
  }

  def logRejections(request: HttpRequest): Any => Option[LogEntry] = {
    case Rejected(rejections) => Some(LogEntry(s"rejecting request $request with $rejections", Logging.ErrorLevel))
    case other => None // not logging other responses
  }
}
