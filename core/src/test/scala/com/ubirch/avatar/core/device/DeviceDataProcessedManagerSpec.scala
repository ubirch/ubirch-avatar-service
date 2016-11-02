package com.ubirch.avatar.core.device

import com.ubirch.avatar.core.test.util.DeviceDataProcessedTestUtil
import com.ubirch.avatar.model.DummyDeviceDataProcessed
import com.ubirch.avatar.model.device.DeviceDataProcessed
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
class DeviceDataProcessedManagerSpec extends ElasticsearchSpec
  with MyJsonProtocol {

  feature("store()") {

    scenario("messageId does not exist") {

      // prepare
      val dataProcessed = DummyDeviceDataProcessed.data()

      // test
      val response = Await.result(DeviceDataProcessedManager.store(dataProcessed), 1 seconds).get
      Thread.sleep(1000)

      // verify
      val dataProcessedList = Await.result(DeviceDataProcessedManager.history(response.deviceId), 1 seconds)
      dataProcessedList.size should be(1)

      dataProcessedList.head should be(response)

    }

    scenario("make sure that messageId is ignore: try to store object with same messageId twice") {

      // prepare
      val dataProcessed1 = DummyDeviceDataProcessed.data()
      val storedDataProcessed1 = Await.result(DeviceDataProcessedManager.store(dataProcessed1), 1 seconds).get

      val dataProcessed2 = DummyDeviceDataProcessed.data(
        deviceId = storedDataProcessed1.deviceId,
        messageId = storedDataProcessed1.messageId
      )

      // test
      val response = Await.result(DeviceDataProcessedManager.store(dataProcessed2), 1 seconds).get
      Thread.sleep(1000)

      // verify
      val dataProcessedList = Await.result(DeviceDataProcessedManager.history(dataProcessed2.deviceId), 1 seconds)
      dataProcessedList.size should be(2)

      dataProcessedList.head should be(response)
      dataProcessedList(1) should be(storedDataProcessed1)

    }

  }

  feature("history()") {

    scenario("deviceId empty") {
      an[IllegalArgumentException] should be thrownBy Await.result(DeviceDataProcessedManager.history(""), 1 seconds)
    }

    scenario("deviceId does not exist; index does not exist") {
      val deviceId = UUIDUtil.uuidStr
      an[ExecutionException] should be thrownBy Await.result(DeviceDataProcessedManager.history(deviceId), 1 seconds)
    }

    scenario("deviceId does not exist; index exists") {

      // prepare
      DeviceDataProcessedTestUtil.storeSeries(1)
      val deviceId = UUIDUtil.uuidStr

      // test
      val result: Seq[DeviceDataProcessed] = Await.result(DeviceDataProcessedManager.history(deviceId), 2 seconds)

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
      val dataSeries: List[DeviceDataProcessed] = DeviceDataProcessedTestUtil.storeSeries(elementCount)
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceDataProcessed] = Await.result(DeviceDataProcessedManager.history(deviceId, from, size), 2 seconds)

      // verify
      result should be('isEmpty)

    }

    scenario("3 records exist: from = 0; size > 3") {

      // prepare
      val elementCount = 3
      val from = 0
      val size = elementCount + 1
      val dataSeries: List[DeviceDataProcessed] = DeviceDataProcessedTestUtil.storeSeries(elementCount).reverse
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceDataProcessed] = Await.result(DeviceDataProcessedManager.history(deviceId, from, size), 2 seconds)

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
      val dataSeries: List[DeviceDataProcessed] = DeviceDataProcessedTestUtil.storeSeries(elementCount).reverse
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceDataProcessed] = Await.result(DeviceDataProcessedManager.history(deviceId, from, size), 2 seconds)

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
      val dataSeries: List[DeviceDataProcessed] = DeviceDataProcessedTestUtil.storeSeries(elementCount)
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceDataProcessed] = Await.result(DeviceDataProcessedManager.history(deviceId, from, size), 2 seconds)

      // verify
      result should be('isEmpty)

    }

  }

  private def testWithInvalidFromOrSize(elementCount: Int, from: Int, size: Int) = {

    // prepare
    val dataSeries: List[DeviceDataProcessed] = DeviceDataProcessedTestUtil.storeSeries(elementCount)
    val deviceId: String = dataSeries.head.deviceId

    // test && verify
    an[IllegalArgumentException] should be thrownBy Await.result(DeviceDataProcessedManager.history(deviceId, from, size), 1 seconds)

  }

}
