package org.nefilim.asgmonitor

import org.scalatest.{FunSpec, Matchers}
import com.typesafe.scalalogging.slf4j.Logging
import spray.testkit.ScalatestRouteTest
import API._
import ASGMonitorAPI._
import spray.routing.HttpService
import spray.http.HttpHeaders
import HttpHeaders._

/**
 * Created by peter on 4/23/14.
 */
class ASGMonitorAPISpec
  extends FunSpec
  with Matchers
  with ScalatestRouteTest
  with ASGMonitorAPI
  with StubServiceActors
  with AkkaExecutionContextProvider
  with HttpService
  with Logging {

  def actorRefFactory = system
  implicit val executionContext = system.dispatcher

  describe("The ASG Monitor API") {
    it("should accept valid notifications") {
      val notificationJson =
        """
          |{
          |  "Type" : "Notification",
          |  "MessageId" : "22b80b92-fdea-4c2c-8f9d-bdfb0c7bf324",
          |  "TopicArn" : "arn:aws:sns:us-east-1:123456789012:MyTopic",
          |  "Subject" : "My First Message",
          |  "Message" : "Hello world!",
          |  "Timestamp" : "2012-05-02T00:54:06.655Z",
          |  "SignatureVersion" : "1",
          |  "Signature" : "EXAMPLEw6JRNwm1LFQL4ICB0bnXrdB8ClRMTQFGBqwLpGbM78tJ4etTwC5zU7O3tS6tGpey3ejedNdOJ+1fkIp9F2/LmNVKb5aFlYq+9rk9ZiPph5YlLmWsDcyC5T+Sy9/umic5S0UQc2PEtgdpVBahwNOdMW4JPwk0kAJJztnc=",
          |  "SigningCertURL" : "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem",
          |  "UnsubscribeURL" : "https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:123456789012:MyTopic:c9135db0-26c4-47ec-8998-413945fb5a96"
          |}
        """.stripMargin

      Post(s"/$ApiRootOU/$ASGMonitorAPIVersion/$ASGEventPath", notificationJson) ~> addHeader("x-amz-sns-message-type", "Notification") ~> eventRoute ~> check {
        handled should be (true)
      }
    }
  }

}
