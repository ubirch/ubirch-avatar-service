package com.ubirch.avatar.core.device

import com.ubirch.avatar.model.{DeviceData, DummyDeviceData}
import com.ubirch.avatar.test.base.ElasticsearchSpec
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-10-25
  */
class DeviceDataManagerSpec extends ElasticsearchSpec
  with MyJsonProtocol {

  feature("history()") {

    scenario("deviceId empty") {
      an[IllegalArgumentException] should be thrownBy Await.result(DeviceDataManager.history(""), 1 seconds)
    }

    scenario("deviceId does not exist") {
      val deviceId = UUIDUtil.uuidStr
      Await.result(DeviceDataManager.history(deviceId), 1 seconds) should be(Seq.empty)
    }

    scenario("3 records exist: from = -1; size > 3") {
      testWithInvalidFromOrSize(elementCount = 3, from = -1, size = 4)
    }

    scenario("3 records exist: from = 0; size = -1") {
      testWithInvalidFromOrSize(elementCount = 3, from = 0, size = -1)
    }

    scenario("3 records exist: from = 0; size = 0") {

      // prepare
      val elementCount = 3
      val from = 0
      val size = 0
      val dataSeries: List[DeviceData] = DummyDeviceData.dataSeries(elementCount = elementCount)
      val deviceId: String = dataSeries.head.deviceId
      store(dataSeries)

      // test
      val result: Seq[DeviceData] = Await.result(DeviceDataManager.history(deviceId, from, size), 2 seconds)

      // verify
      result should be('isEmpty)

    }

    scenario("3 records exist: from = 0; size > 3") {

      // prepare
      val elementCount = 3
      val from = 0
      val size = elementCount + 1
      val dataSeries: List[DeviceData] = DummyDeviceData.dataSeries(elementCount = elementCount)
      val deviceId: String = dataSeries.head.deviceId
      store(dataSeries)

      // test
      val result: Seq[DeviceData] = Await.result(DeviceDataManager.history(deviceId, from, size), 2 seconds)

      // verify
      result.size should be(3)
      result should be('nonEmpty)
      // TODO check order of elements

    }

    ignore("3 records exist: from = 1; size > 3") {
      // TODO write test
    }

    ignore("3 records exist: from = 3; size > 3") {
      // TODO write test
    }

  }

  private def store(dataSeries: List[DeviceData]) = {

    dataSeries foreach { deviceData =>
      DeviceDataManager.store(deviceData)
    }
    Thread.sleep(3000)

  }

  private def testWithInvalidFromOrSize(elementCount: Int, from: Int, size: Int) = {

    // prepare
    val dataSeries: List[DeviceData] = DummyDeviceData.dataSeries(elementCount = elementCount)
    val deviceId: String = dataSeries.head.deviceId
    store(dataSeries)

    // test && verify
    an[IllegalArgumentException] should be thrownBy Await.result(DeviceDataManager.history(deviceId, from, size), 1 seconds)

  }

}
