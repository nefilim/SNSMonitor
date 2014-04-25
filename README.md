SNSMonitor
==========

HTTP(s) service to consume SNS Notifications. The primary motivation for creating this is to keep Chef's view in sync with EC2 Auto Scale groups (created by CloudFormation). 

Depends on https://github.com/nefilim/ScalaChefClient. A HTTP(s) subscription needs to be added to the SNS topic (that is associated with your AutoScale group) that points to the endpoint exposed by this service. Note that SNS doesn't appear to support VPC nodes, so it must be a public, for instance: 

http://54.54.23.23:8080/snsmonitor/v1/event

Once you've added the subscription, SNS will send a SubscriptionConfirmation to the registered endpoint, it expects the endpoint to do a GET on the supplied callback URL to confirm the subscription. 

Integrated **x-amz-sns-message-type** types:

* SubscriptionConfirmation - automatically does the callback to the provided callback URL to confirm the subscription
* Notification - only events *autoscaling:EC2_INSTANCE_TERMINATE* are processed, others are ignored

**_EC2_INSTANCE_TERMINATE_**

The provided Instance_Id is queried with the Chef client, the resulting node is then deleted as well as the accompanying chef client. 
**Note** it is assumed that your Ohai is hinted with ec2 so that the ec2 attributes are populated on the node. It will search on the *instance_id* attribute under *ec2*. 
