package com.ubirch.avatar.core.device

import com.ubirch.avatar.core.test.util.DeviceDataRawTestUtil
import com.ubirch.avatar.model.DummyDeviceDataRaw
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.avatar.test.base.ElasticsearchSpec
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionException}
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
      val deviceDataRaw = DummyDeviceDataRaw.data()

      // test
      val storedDeviceDataRaw1 = Await.result(DeviceDataRawManager.store(deviceDataRaw), 1 seconds).get
      Thread.sleep(1000)

      // verify
      val deviceDataRawList = Await.result(DeviceDataRawManager.history(storedDeviceDataRaw1.deviceId), 1 seconds)
      deviceDataRawList.size should be(1)

      val deviceDataRawInDb = deviceDataRawList.head
      deviceDataRawInDb should be(storedDeviceDataRaw1)

    }

    scenario("make sure that messageId is ignore: try to store object with same messageId twice") {

      // prepare
      val deviceDataRaw1 = DummyDeviceDataRaw.data()
      val storedDeviceDataRaw1 = Await.result(DeviceDataRawManager.store(deviceDataRaw1), 1 seconds).get

      val deviceDataRaw2 = DummyDeviceDataRaw.data(
        deviceId = storedDeviceDataRaw1.deviceId,
        messageId = storedDeviceDataRaw1.messageId
      )

      // test
      val storedDeviceDataRaw2 = Await.result(DeviceDataRawManager.store(deviceDataRaw2), 1 seconds).get
      Thread.sleep(1000)

      // verify
      val deviceDataRawList = Await.result(DeviceDataRawManager.history(deviceDataRaw2.deviceId), 1 seconds)
      deviceDataRawList.size should be(2)

      deviceDataRawList.head should be(storedDeviceDataRaw2)
      deviceDataRawList(1) should be(storedDeviceDataRaw1)

    }

  }

  feature("history()") {

    scenario("deviceId empty") {
      an[IllegalArgumentException] should be thrownBy Await.result(DeviceDataRawManager.history(""), 1 seconds)
    }

    scenario("deviceId does not exist; index does not exist") {
      val deviceId = UUIDUtil.uuidStr
      an[ExecutionException] should be thrownBy Await.result(DeviceDataRawManager.history(deviceId), 1 seconds)
    }

    scenario("deviceId does not exist; index exists") {

      // prepare
      DeviceDataRawTestUtil.storeSeries(1)
      val deviceId = UUIDUtil.uuidStr

      // test
      val result: Seq[DeviceDataRaw] = Await.result(DeviceDataRawManager.history(deviceId), 2 seconds)

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
      val dataSeries: List[DeviceDataRaw] = DeviceDataRawTestUtil.storeSeries(elementCount)
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceDataRaw] = Await.result(DeviceDataRawManager.history(deviceId, from, size), 2 seconds)

      // verify
      result should be('isEmpty)

    }

    scenario("3 records exist: from = 0; size > 3") {

      // prepare
      val elementCount = 3
      val from = 0
      val size = elementCount + 1
      val dataSeries: List[DeviceDataRaw] = DeviceDataRawTestUtil.storeSeries(elementCount).reverse
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceDataRaw] = Await.result(DeviceDataRawManager.history(deviceId, from, size), 2 seconds)

      // verify
      result.size should be(3)
      for (i <- dataSeries.indices) {
        result(i) shouldEqual dataSeries(i)
      }

    }

    scenario("3 records exist: from = 1; size > 3") {

      // prepare
      val elementCount = 3
      val from = 1
      val size = elementCount + 1
      val dataSeries: List[DeviceDataRaw] = DeviceDataRawTestUtil.storeSeries(elementCount).reverse
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceDataRaw] = Await.result(DeviceDataRawManager.history(deviceId, from, size), 2 seconds)

      // verify
      result.size should be(2)
      result.head shouldEqual dataSeries(1)
      result(1) shouldEqual dataSeries(2)

    }

    scenario("3 records exist: from = 3; size > 3") {

      // prepare
      val elementCount = 3
      val from = elementCount
      val size = elementCount + 1
      val dataSeries: List[DeviceDataRaw] = DeviceDataRawTestUtil.storeSeries(elementCount)
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceDataRaw] = Await.result(DeviceDataRawManager.history(deviceId, from, size), 2 seconds)

      // verify
      result should be('isEmpty)

    }

  }

  private def testWithInvalidFromOrSize(elementCount: Int, from: Int, size: Int) = {

    // prepare
    val dataSeries: List[DeviceDataRaw] = DeviceDataRawTestUtil.storeSeries(elementCount)
    val deviceId: String = dataSeries.head.deviceId

    // test && verify
    an[IllegalArgumentException] should be thrownBy Await.result(DeviceDataRawManager.history(deviceId, from, size), 1 seconds)

  }

}
