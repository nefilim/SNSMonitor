package org.nefilim.snsmonitor

import akka.actor._
import com.typesafe.scalalogging.slf4j.Logging
import akka.io.IO
import spray.can.Http
import scala.language.postfixOps
import org.nefilim.generated.BuildInfo
import org.nefilim.snsmonitor.service.SNSMonitorAPIWorker


/**
 * Created by peter on 3/16/14.
 */
trait SystemProvider {
  implicit val system = ActorSystem("SNSMonitorActorSystem")
}

object Boot extends App with SystemProvider with Logging{
  run()
  def run() {
    logger.info("Starting [{}] version [{}] buildTime [{}]", BuildInfo.name, BuildInfo.version, BuildInfo.buildTime)
    logger.info("boot running with system {}", system)

    val service = system.actorOf(SNSMonitorAPIWorker.props(), "SNSMonitorAPIWorkerActor")
    IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = 8080)
  }
}

class JsvcWrapper extends Logging {

  def init(arguments: Array[String]) {
    logger.info("JsvcWrapper.init")
  }

  def start() {
    logger.info("JsvcWrapper.start")
    Boot.run()
  }

  def stop() {
    logger.info("JsvcWrapper.stop")
    Boot.system.shutdown()
  }

  def destroy() {
    logger.info("JsvcWrapper.destroy")
  }

}