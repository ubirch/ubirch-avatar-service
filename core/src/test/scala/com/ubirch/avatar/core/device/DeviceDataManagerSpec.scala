package com.ubirch.avatar.core.device

import com.ubirch.avatar.test.base.UnitSpec
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-10-25
  */
class DeviceDataManagerSpec extends UnitSpec
  with MyJsonProtocol {

  feature("history()") {

    scenario("deviceId empty") {
      an[IllegalArgumentException] should be thrownBy Await.result(DeviceDataManager.history(""), 1 seconds)
    }

    scenario("deviceId does not exist") {
      val deviceId = UUIDUtil.uuidStr
      Await.result(DeviceDataManager.history(deviceId), 1 seconds) should be(Seq.empty)
    }

    ignore("3 records exist: from = -1; size > 3") {
      // TODO write test
    }

    ignore("3 records exist: from = 0; size > 3") {
      // TODO write test
    }

    ignore("3 records exist: from = 0; size = 0") {
      // TODO write test
    }

    ignore("3 records exist: from = 1; size > 3") {
      // TODO write test
    }

    ignore("3 records exist: from = 3; size > 3") {
      // TODO write test
    }

  }

}
