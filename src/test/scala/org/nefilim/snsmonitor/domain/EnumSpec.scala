package org.nefilim.snsmonitor.domain

import org.scalatest.{Matchers, FunSpec}
import com.typesafe.scalalogging.slf4j.Logging
import Internal._

/**
 * Created by peter on 5/1/14.
 */
class EnumSpec extends FunSpec with Matchers with Logging {

  describe("test enum functionality") {
    it("should return the correct instance") {
      AutoScalingNotificationType.fromString("autoscaling:EC2_INSTANCE_TERMINATE") should be(Some(AutoScalingNotificationType.EC2InstanceTerminate))
    }
  }
}
