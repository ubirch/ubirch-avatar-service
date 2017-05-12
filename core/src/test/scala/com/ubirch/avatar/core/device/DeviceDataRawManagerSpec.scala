package com.ubirch.avatar.core.device

import com.ubirch.avatar.core.test.util.DeviceDataRawTestUtil
import com.ubirch.avatar.model.rest.device.{Device, DeviceDataRaw}
import com.ubirch.avatar.model.{DummyDeviceDataRaw, DummyDevices}
import com.ubirch.avatar.test.base.ElasticsearchSpec
import com.ubirch.util.json.MyJsonProtocol

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-10-25
  */
class DeviceDataRawManagerSpec extends ElasticsearchSpec
  with MyJsonProtocol {

  feature("store()") {

    scenario("messageId does not exist") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val rawData = DummyDeviceDataRaw.data(device = device)()

      // test
      val storedRaw1 = Await.result(DeviceDataRawManager.store(rawData), 1 seconds).get
      Thread.sleep(1200)

      // verify
      val expectedStoredRaw = rawData.copy(id = storedRaw1.id)
      storedRaw1 should be(expectedStoredRaw)

      val deviceDataRawInDb = Await.result(DeviceDataRawManager.loadById(storedRaw1.id), 1 seconds)
      Some(storedRaw1) should be(deviceDataRawInDb)

    }

    scenario("make sure that messageId is ignored: try to store object with same messageId twice") {

      // prepare
      val device = DummyDevices.minimalDevice()

      val rawData1 = DummyDeviceDataRaw.data(device = device)()
      val storedRaw1 = Await.result(DeviceDataRawManager.store(rawData1), 1 seconds).get

      val rawData2 = DummyDeviceDataRaw.data(device = device, messageId = storedRaw1.id)()

      // test
      val storedRaw2 = Await.result(DeviceDataRawManager.store(rawData2), 1 seconds).get
      Thread.sleep(1200)

      // verify
      val deviceDataRawList = Await.result(DeviceDataRawManager.history(device), 1 seconds)
      deviceDataRawList.size should be(2)

      deviceDataRawList.head should be(storedRaw2)
      deviceDataRawList(1) should be(storedRaw1)

    }

  }

  feature("history()") {

    scenario("deviceId empty") {
      val device = DummyDevices.minimalDevice(hwDeviceId = "")
      an[IllegalArgumentException] should be thrownBy Await.result(DeviceDataRawManager.history(device), 1 seconds)
    }

    scenario("deviceId does not exist; index does not exist") {
      deleteIndices()
      val device = DummyDevices.minimalDevice()
      Await.result(DeviceDataRawManager.history(device), 1 seconds) should be('isEmpty)
    }

    scenario("deviceId does not exist; index exists") {

      // prepare
      DeviceDataRawTestUtil.storeSeries(1)
      val device = DummyDevices.minimalDevice()

      // test
      val result: Seq[DeviceDataRaw] = Await.result(DeviceDataRawManager.history(device), 2 seconds)

      // verify
      result should be('isEmpty)

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
      val (device: Device, _) = DeviceDataRawTestUtil.storeSeries(elementCount)

      // test
      val result: Seq[DeviceDataRaw] = Await.result(DeviceDataRawManager.history(device, from, size), 2 seconds)

      // verify
      result should be('isEmpty)

    }

    scenario("3 records exist: from = 0; size > 3") {

      // prepare
      val elementCount = 3
      val from = 0
      val size = elementCount + 1
      val (device: Device, dataSeries: List[DeviceDataRaw]) = DeviceDataRawTestUtil.storeSeries(elementCount)

      // test
      val result: Seq[DeviceDataRaw] = Await.result(DeviceDataRawManager.history(device, from, size), 2 seconds)

      // verify
      result.size should be(3)
      for (i <- dataSeries.indices) {
        result(i) shouldEqual dataSeries.reverse(i)
      }

    }

    scenario("3 records exist: from = 1; size > 3") {

      // prepare
      val elementCount = 3
      val from = 1
      val size = elementCount + 1
      val (device: Device, dataSeries: List[DeviceDataRaw]) = DeviceDataRawTestUtil.storeSeries(elementCount)

      // test
      val result: Seq[DeviceDataRaw] = Await.result(DeviceDataRawManager.history(device, from, size), 2 seconds)

      // verify
      result.size should be(2)
      result.head shouldEqual dataSeries.reverse(1)
      result(1) shouldEqual dataSeries.reverse(2)

    }

    scenario("3 records exist: from = 3; size > 3") {

      // prepare
      val elementCount = 3
      val from = elementCount
      val size = elementCount + 1
      val (device: Device, _) = DeviceDataRawTestUtil.storeSeries(elementCount)

      // test
      val result: Seq[DeviceDataRaw] = Await.result(DeviceDataRawManager.history(device, from, size), 2 seconds)

      // verify
      result should be('isEmpty)

    }

  }

  private def testWithInvalidFromOrSize(elementCount: Int, from: Int, size: Int) = {

    // prepare
    val (device: Device, _) = DeviceDataRawTestUtil.storeSeries(elementCount)

    // test && verify
    an[IllegalArgumentException] should be thrownBy Await.result(DeviceDataRawManager.history(device, from, size), 1 seconds)

  }

}
