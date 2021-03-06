package org.nefilim.snsmonitor.service

import akka.testkit.{TestActorRef, TestKit}
import akka.actor.ActorSystem
import com.typesafe.scalalogging.slf4j.Logging
import org.scalatest._
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.util.Timeout
import scala.language.postfixOps
import spray.json._
import org.nefilim.snsmonitor.domain.API._
import MyJsonProtocol._
import com.amazonaws.services.glacier.model.StatusCode
import spray.http.StatusCodes
import akka.pattern.ask

class MonitorServiceSpec
  extends TestKit(ActorSystem("testsystem"))
  with Logging
  with FunSpecLike
  with Matchers {

  implicit val timeout = Timeout(5 seconds)

  val rawSNSNotification =
    """
      |{
      |  "Type" : "Notification",
      |  "MessageId" : "05e7c3d3-4a38-5884-b8a0-880067ed4515",
      |  "TopicArn" : "arn:aws:sns:us-west-2:1234512345:ToastyPipeRC3-NotificationTopic-19QU7X525MXD6",
      |  "Subject" : "Auto Scaling: termination for group \"ToastyPipeRC3-AutoScaleGroupCollector-1MS9NXK6J46QI\"",
      |  "Message" : "{\"StatusCode\":\"InProgress\",\"Service\":\"AWS Auto Scaling\",\"AutoScalingGroupName\":\"ToastyPipeRC3-AutoScaleGroupCollector-1MS9NXK6J46QI\",\"Description\":\"Terminating EC2 instance: i-0f662d07\",\"ActivityId\":\"701fd66a-21e3-4fd6-b085-b9ad1a8284b7\",\"Event\":\"autoscaling:EC2_INSTANCE_TERMINATE\",\"Details\":{\"Availability Zone\":\"us-west-2c\"},\"AutoScalingGroupARN\":\"arn:aws:autoscaling:us-west-2:1234512345:autoScalingGroup:90e317d4-9a9c-4230-b0a6-04891dfb79e0:autoScalingGroupName/ToastyPipeRC3-AutoScaleGroupCollector-1MS9NXK6J46QI\",\"Progress\":50,\"Time\":\"2014-04-24T15:19:49.586Z\",\"AccountId\":\"1234512345\",\"RequestId\":\"701fd66a-21e3-4fd6-b085-b9ad1a8284b7\",\"StatusMessage\":\"\",\"EndTime\":\"2014-04-24T15:19:49.586Z\",\"EC2InstanceId\":\"i-0f662d07\",\"StartTime\":\"2014-04-24T15:19:31.820Z\",\"Cause\":\"At 2014-04-24T15:19:31Z an instance was taken out of service in response to a system health-check.\"}",
      |  "Timestamp" : "2014-04-24T15:19:49.623Z",
      |  "SignatureVersion" : "1",
      |  "Signature" : "nTrNHPm1BgmAwyLv89fvdtx8pb+LXwsjkus+DVHgCnCbCeL3G6ywRcK7+/5NuS5r2wD+4EzZK8TMNvka3CYEhgPUDvgr3aLXvjKkLDopTjmbVsI+UXcxRGCGBlveb+GwgDJJhg/pd6TJSAM+dBMwrFlQNZ8VSsCQYrRVu3abMk7noJu3VeWHMxEceYvGJYNw74TU1rvW1ZqIL95mrYW2HCd6W7iUbedjjsLBRSM2x63QPBppHbM1achII4DawSCZcmzEQs2thE7uFtFbTHzJr9pyDR4gOSzQBbmSP2dgm807biGVGffIOm8qI2+vQTBFzX6pOZwbcjcI8q9Gskff/Q==",
      |  "SigningCertURL" : "https://sns.us-west-2.amazonaws.com/SimpleNotificationService-e372f8ca30337fdb084e8ac449342c77.pem",
      |  "UnsubscribeURL" : "https://sns.us-west-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-west-2:1234512345:ToastyPipeRC3-NotificationTopic-19QU7X525MXD6:c8407925-7072-4b25-a0fe-d5d9afb33e97"
      |}
    """.stripMargin

  val minimalNotification =
  """
    |{
    |  "Type" : "Notification",
    |  "MessageId" : "22559f04-d218-52ee-89a7-d4dd6b556719",
    |  "TopicArn" : "arn:aws:sns:us-west-2:1234512345:AutoScale_LifeCycle_Events",
    |  "Subject" : "Auto Scaling: test notification for group \"ToastyPipe-AutoScaleGroupCollector-1KWGEG2RPLOHL\"",
    |  "Message" : "{\"AutoScalingGroupName\":\"ToastyPipe-AutoScaleGroupCollector-1KWGEG2RPLOHL\",\"Service\":\"AWS Auto Scaling\",\"Time\":\"2014-04-29T14:28:29.690Z\",\"AccountId\":\"1234512345\",\"Event\":\"autoscaling:TEST_NOTIFICATION\",\"RequestId\":\"88bf6069-cfaa-11e3-a2dc-cd20eb22172d\",\"AutoScalingGroupARN\":\"arn:aws:autoscaling:us-west-2:1234512345:autoScalingGroup:3bb92dc2-b6ab-4240-8ae2-759954df90a4:autoScalingGroupName/ToastyPipe-AutoScaleGroupCollector-1KWGEG2RPLOHL\"}",
    |  "Timestamp" : "2014-04-29T14:28:29.734Z",
    |  "SignatureVersion" : "1",
    |  "Signature" : "TqPr8TepNM3zSigsl3SqyTSPiJavDExyPiWypVX5irIdZyFGLc72NZCJdDzIiO8YIjVOwzFuLNGka/FLo97Dr3tNIdiQ/fcelm4Cyw1FfsZZeYqc3VKmcVxW50M7sYQEUMKV71N3/pT6pJBdPDGkDggBYK07VcDZUQEiFHPZOxDdfCBL0y7bfHh+5egjEVAZxlnQOKdDTBnlIxqMtPXWP+ZEK1jhKXM4H/EmDN1nQ6AIKCL5/uOjcy/m6BpjVFaCQoAFo6H9D1Sx131snXHm42nkMaXe6VDGR4eYQvEE8EsgJmA3tW/GRtSysaX/U45jKuJJsdYsanyL9q3RU5zysQ==",
    |  "SigningCertURL" : "https://sns.us-west-2.amazonaws.com/SimpleNotificationService-e372f8ca30337fdb084e8ac449342c77.pem",
    |  "UnsubscribeURL" : "https://sns.us-west-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-west-2:1234512345:AutoScale_LifeCycle_Events:f56a91e3-d16f-419b-9392-cf02786f3f75"
    |}
  """.stripMargin

  describe("process SNS notifications") {
    it("should process auto scaling terminate event") {
      val chefClient = new StubChefClient()

      val monitorService = TestActorRef[MonitorService](MonitorService.props(chefClient))

      val json = JsonParser(rawSNSNotification)
      val notification = json.convertTo[SNSNotification]
      val result = Await.result((monitorService ? notification).mapTo[(StatusCode, String)], timeout.duration)
      result._1 should be (StatusCodes.OK)
      logger.info("GOT RESULT from monitorservice {}", result)
    }

    it("should not break on minimal notification") {
      val chefClient = new StubChefClient()

      val monitorService = TestActorRef[MonitorService](MonitorService.props(chefClient))

      val json = JsonParser(minimalNotification)
      val notification = json.convertTo[SNSNotification]
      val result = Await.result((monitorService ? notification).mapTo[(StatusCode, String)], timeout.duration)
      result._1 should be (StatusCodes.OK)
      logger.info("GOT RESULT from monitorservice {}", result)
    }
  }
}
