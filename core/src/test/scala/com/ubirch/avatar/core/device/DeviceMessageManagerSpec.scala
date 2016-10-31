package com.ubirch.avatar.core.device

import com.ubirch.avatar.core.test.util.DeviceMessageTestUtil
import com.ubirch.avatar.model.DummyDeviceMessage
import com.ubirch.avatar.model.device.DeviceMessage
import com.ubirch.avatar.test.base.ElasticsearchSpec
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.{ExecutionException, Await}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-10-25
  */
class DeviceMessageManagerSpec extends ElasticsearchSpec
  with MyJsonProtocol {

  feature("store()") {

    scenario("messageId does not exist") {

      // prepare
      val deviceMessage = DummyDeviceMessage.data()

      // test
      Await.result(DeviceMessageManager.store(deviceMessage), 1 seconds)
      Thread.sleep(1000)

      // verify
      val deviceMsgList = Await.result(DeviceMessageManager.history(deviceMessage.deviceId), 1 seconds)
      deviceMsgList.size should be(1)

      val deviceMsgInDb = deviceMsgList.head
      deviceMsgInDb should be(deviceMessage)

    }

    scenario("messageId already exist") {

      // prepare
      val deviceMsg1 = DummyDeviceMessage.data()
      Await.result(DeviceMessageManager.store(deviceMsg1), 1 seconds)

      val deviceMsg2 = DummyDeviceMessage.data(deviceId = deviceMsg1.deviceId, messageId = deviceMsg1.messageId)

      // test
      Await.result(DeviceMessageManager.store(deviceMsg2), 1 seconds)
      Thread.sleep(1000)

      // verify
      val deviceMsgList = Await.result(DeviceMessageManager.history(deviceMsg2.deviceId), 1 seconds)
      deviceMsgList.size should be(1)

      val deviceMsgInDb = deviceMsgList.head
      deviceMsgInDb should be(deviceMsg2)

    }

  }

  feature("history()") {

    scenario("deviceId empty") {
      an[IllegalArgumentException] should be thrownBy Await.result(DeviceMessageManager.history(""), 1 seconds)
    }

    scenario("deviceId does not exist; index does not exist") {
      val deviceId = UUIDUtil.uuidStr
      an[ExecutionException] should be thrownBy Await.result(DeviceMessageManager.history(deviceId), 1 seconds)
    }

    scenario("deviceId does not exist; index exists") {

      // prepare
      DeviceMessageTestUtil.storeSeries(1)
      val deviceId = UUIDUtil.uuidStr

      // test
      val result: Seq[DeviceMessage] = Await.result(DeviceMessageManager.history(deviceId), 2 seconds)

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
      val dataSeries: List[DeviceMessage] = DeviceMessageTestUtil.storeSeries(elementCount)
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceMessage] = Await.result(DeviceMessageManager.history(deviceId, from, size), 2 seconds)

      // verify
      result should be('isEmpty)

    }

    scenario("3 records exist: from = 0; size > 3") {

      // prepare
      val elementCount = 3
      val from = 0
      val size = elementCount + 1
      val dataSeries: List[DeviceMessage] = DeviceMessageTestUtil.storeSeries(elementCount).reverse
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceMessage] = Await.result(DeviceMessageManager.history(deviceId, from, size), 2 seconds)

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
      val dataSeries: List[DeviceMessage] = DeviceMessageTestUtil.storeSeries(elementCount).reverse
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceMessage] = Await.result(DeviceMessageManager.history(deviceId, from, size), 2 seconds)

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
      val dataSeries: List[DeviceMessage] = DeviceMessageTestUtil.storeSeries(elementCount)
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceMessage] = Await.result(DeviceMessageManager.history(deviceId, from, size), 2 seconds)

      // verify
      result should be('isEmpty)

    }

  }

  private def testWithInvalidFromOrSize(elementCount: Int, from: Int, size: Int) = {

    // prepare
    val dataSeries: List[DeviceMessage] = DeviceMessageTestUtil.storeSeries(elementCount)
    val deviceId: String = dataSeries.head.deviceId

    // test && verify
    an[IllegalArgumentException] should be thrownBy Await.result(DeviceMessageManager.history(deviceId, from, size), 1 seconds)

  }

}
