package org.nefilim.snsmonitor.asg

import org.nefilim.chefclient.ChefClient
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.{Promise, Future}
import org.nefilim.chefclient.domain.ChefConstructs._
import org.nefilim.chefclient.domain.ChefConstructs.NodeIndexResultRow
import org.nefilim.chefclient.domain.ChefConstructs.ChefSearchResult
import org.nefilim.chefclient.domain.ChefConstructs.ChefNode
import scala.Some
import org.nefilim.chefclient.domain.ChefConstructs.OhaiValues
import spray.http.HttpResponse

/**
 * Created by peter on 4/24/14.
 */

class StubChefClient() extends ChefClient with Logging {
  override def nodeList(): Future[Either[ChefClientFailedResult, List[ChefNode]]] = {
    val result = List(ChefNode("node1", "http://manage.opscode.com/organization/test/node/node1"))
    Promise[Either[ChefClientFailedResult, List[ChefNode]]].success(Right(result)).future
  }

  override def searchNodeIndex(query: String, start: Int, rows: Int, sort: String): Future[Either[ChefClientFailedResult, ChefSearchResult[NodeIndexResultRow]]] = {
    val result = ChefSearchResult(1, 0, Set[NodeIndexResultRow](NodeIndexResultRow("node1-i-0f662d07", "production", OhaiValues("linux", "3.10.34-37.137.amzn1.x86_64", "ip-10-0-20-232", "ip-10-0-20-232.us-west-2.compute.internal", "us-west-2.compute.internal", "10.0.20.232", 0, Set("service", "java7"), Some(EC2Values("ami-b8f69f88", "/dev/sda1", "/dev/sda1", "ip-10-0-20-232.us-west-2.compute.internal", "none", "i-0f662d07", "aki-fc8f11cc", "0a:22:72:e2:e2:19"))), "Chef::Node", "node", List("role[app]"))))
    Promise[Either[ChefClientFailedResult, ChefSearchResult[NodeIndexResultRow]]].success(Right(result)).future
  }

  override def deleteNode(node: String): Future[Either[ChefClientFailedResult, LastKnownNodeState]] = {
    val lastKnownNodeState = LastKnownNodeState("node1", "node", "Chef::Node", Some("production"))
    Promise[Either[ChefClientFailedResult, LastKnownNodeState]].success(Right(lastKnownNodeState)).future
  }

  override def deleteClient(client: String): Future[Either[ChefClientFailedResult, HttpResponse]] = {
    Promise[Either[ChefClientFailedResult, HttpResponse]].success(Right(HttpResponse())).future
  }
}
