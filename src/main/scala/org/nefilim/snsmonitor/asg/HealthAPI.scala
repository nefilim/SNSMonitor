package org.nefilim.snsmonitor.asg

import spray.routing._
import com.typesafe.scalalogging.slf4j.Logging
import org.nefilim.snsmonitor.asg.API._
import spray.routing.Directives._

/**
 * Created by peter on 4/27/14.
 */
object HealthAPI extends Logging {
  private[asg] val HealthAPIVersion = "v1"
  private[asg] val HealthPath = "health"

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
