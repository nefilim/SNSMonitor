SNSMonitor
==========

HTTP(s) service to consume SNS Notifications. The primary motivation for creating this is to keep Chef's view in sync with EC2 Auto Scale groups (created by CloudFormation). 

Depends on https://github.com/nefilim/ScalaChefClient. A HTTP(s) subscription needs to be added to the SNS topic (that is associated with your AutoScale group) that points to the endpoint exposed by this service. Note that SNS doesn't appear to support VPC nodes, so it must be a public (or the ELB in front of it at least), for instance: 

http://54.54.23.23:8080/snsmonitor/v1/event

Once you've added the subscription, SNS will send a SubscriptionConfirmation to the registered endpoint, it expects the endpoint to do a GET on the supplied callback URL to confirm the subscription. 

Integrated **x-amz-sns-message-type** types:

* SubscriptionConfirmation - automatically does the callback to the provided callback URL to confirm the subscription
* Notification - only events *autoscaling:EC2_INSTANCE_TERMINATE* are processed, others are ignored

**_EC2_INSTANCE_TERMINATE_**

The provided Instance_Id is queried with the Chef client, the resulting node is then deleted as well as the accompanying chef client. 
**Note** it is assumed that your Ohai is hinted with ec2 so that the ec2 attributes are populated on the node. It will search on the *instance_id* attribute under *ec2*. 

Installation
---

```
git clone https://github.com/nefilim/SNSMonitor.git
sbt clean universal:packageZipTarball
```
Copy the snsmonitor-0.1.tgz to an accessible S3 bucket.

```
cd SNSMonitor/src/main/
tar cvfz cookbooks.tar.gz cookbooks
```
Upload cookbooks.tar.gz to S3 bucket.

Included is a CloudFormation template (```src/main/cloudformation```) and Chef cookbook (```src/main/cookbooks```) to spin up a 2 node AutoScale group in a VPC fronted by an ELB. The snsmonitor cookbook depends on the Java cookbook (https://github.com/socrata-cookbooks/java) that is included here also. 

Installation locations: 
* binaries (jars): ```/usr/local/snsmonitor/```
* app & logging config under: ```/etc/snsmonitor/```
* logging under: ```/var/log/snsmonitor/```

The following parameters needs to be set on the CloudFormation template (either in the template as default values or in the CF console when creating the stack):

* ***SNSMonitorBinaryURL*** pointing to the S3 object uploaded
* ***ChefCookbookURL*** S3 object containing cookbooks.tar.gz
* ***ChefUserId*** your chef user to use the Chef Server API with
* ***ChefKeyPath*** path to your private key on the instances eg /etc/snsmonitor/client.pem
* ***ChefOrganization*** your chef organization
* ***VpcId*** your VPC id
* ***NodeSubnets*** your "private" VPC subnets for the instances to go into, by default it needs a subnet in each AZ, you will need to modify the template if you dont
* ***LBSubnets*** your VPC subnets for the ELB to go into, needs to be a "public" VPC subnet


Right now you have to make your own plan to get your keys on the your instances. 

Standalone Configuration
---

see ```src/main/config/application.conf``` for configuring the chef client. It also supports an optional parameter *organization* in the chef stanza for enterprise chef. 

Specify the location with ```-Dconfig.file=/path/application.conf```.


