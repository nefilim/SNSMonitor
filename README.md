SNSMonitor
==========

HTTP(s) service to consume SNS Notifications. The primary motivation for creating this is to keep Chef's view in sync with EC2 Auto Scale groups (created by CloudFormation). 

Depends on https://github.com/nefilim/ScalaChefClient

Integrated **x-amz-sns-message-type** types:

* SubscriptionConfirmation - does the callback to the provided callback URL to confirm the subscription
* Notification - only events *autoscaling:EC2_INSTANCE_TERMINATE* are processed, others are ignored

**_EC2_INSTANCE_TERMINATE_**

The provided Instance_Id is queried with the Chef client, the resulting node is then deleted as well as the accompanying chef client. 
