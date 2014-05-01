package org.nefilim.snsmonitor.domain

import spray.json.DefaultJsonProtocol

/**
 * Created by peter on 5/1/14.
 */
object API {

  case class SNSSubscriptionConfirm(
               Message: String,
               MessageId: String,
               Signature: String,
               SignatureVersion: String,
               SigningCertURL: String,
               SubscribeURL: String,
               Timestamp: String,
               Token: String,
               TopicArn: String,
               Type: String)

  case class SNSNotification(
               Message: String,
               MessageId: String,
               Signature: String,
               SignatureVersion: String,
               SigningCertURL: String,
               Subject: Option[String],
               Timestamp: String,
               TopicArn: String,
               Type: String,
               UnsubscribeURL: String)

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val snsNotificationFormat = jsonFormat10(SNSNotification)
    implicit val snsSubscriptionConfirmFormat = jsonFormat10(SNSSubscriptionConfirm)
  }

}
