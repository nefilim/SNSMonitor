package org.nefilim.snsmonitor.api

import spray.routing._
import com.typesafe.scalalogging.slf4j.Logging
import spray.routing.Directives._
import API._

/**
 * Created by peter on 4/27/14.
 */
object HealthAPI extends Logging {
  private[api] val HealthAPIVersion = "v1"
  private[api] val HealthPath = "health"

  def healthRouteBase(innerRoute: Route): Route = {
    apiRoot {
      pathPrefix(HealthAPIVersion) {
        innerRoute
      }
    }
  }
}
import HealthAPI._

trait HealthAPI extends HttpService with Logging {
  val healthRoute =
    healthRouteBase {
      path(HealthPath) {
        get {
          complete {
            "OK"
          }
        }
      }
    }
}
