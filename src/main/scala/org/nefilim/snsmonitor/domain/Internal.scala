package org.nefilim.snsmonitor.domain

import spray.json.DefaultJsonProtocol

/**
 * Created by peter on 5/1/14.
 */
object Internal {

  trait MonitorEvent

  case class AutoScalingNotification(
               StatusCode: Option[String],
               Service: String,
               AutoScalingGroupName: String,
               Description: Option[String],
               ActivityId: Option[String],
               Event: String,
               AutoScalingGroupARN: String,
               Progress: Option[Int],
               Time: String,
               AccountId: String,
               RequestId: String,
               StatusMessage: Option[String],
               EndTime: Option[String],
               EC2InstanceId: Option[String],
               StartTime: Option[String],
               Cause: Option[String]) extends MonitorEvent

  object AutoScalingNotificationType extends Enumeration {
    val EC2InstanceLaunch = Value("autoscaling:EC2_INSTANCE_LAUNCH".toUpperCase)
    val EC2InstanceLaunchError = Value("autoscaling:EC2_INSTANCE_LAUNCH_ERROR".toUpperCase)
    val EC2InstanceTerminate = Value("autoscaling:EC2_INSTANCE_TERMINATE".toUpperCase)
    val EC2InstanceTerminateError = Value("autoscaling:EC2_INSTANCE_TERMINATE_ERROR".toUpperCase)
    val EC2TestNotification = Value("autoscaling:TEST_NOTIFICATION".toUpperCase)

    def fromString(input: String): Option[AutoScalingNotificationType.Value] = {
      try {
        Some(AutoScalingNotificationType.withName(input.toUpperCase))
      } catch {
        case e:Exception =>
          None
      }
    }
  }

  object InternalJsonProtocol extends DefaultJsonProtocol {
    implicit val AWSAutoScalingNotificationFormat = jsonFormat16(AutoScalingNotification)
  }
}
