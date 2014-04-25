package org.nefilim.snsmonitor.asg

import org.scalatest.FunSpec
import com.typesafe.scalalogging.slf4j.Logging
import spray.http.{HttpRequest, HttpResponse}
import scala.concurrent.{Await, Future}
import spray.testkit.ScalatestRouteTest
import spray.routing.HttpService
import spray.client.pipelining._
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created by peter on 4/24/14.
 */
class ClientTest extends FunSpec with Logging with ScalatestRouteTest with HttpService {

  def actorRefFactory = system //

  describe("connect to unconnectable host") {
    it("should timeout") {
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

      val response: Future[HttpResponse] = pipeline(Get("http://4.2.2.2/"))
      response.onComplete {
        case Success(result) =>
          logger.info("result {}", result)
        case Failure(f) =>
          logger.error("failed to connect", f)

      }

      Await.ready(response, (30 seconds))
    }
  }

}
