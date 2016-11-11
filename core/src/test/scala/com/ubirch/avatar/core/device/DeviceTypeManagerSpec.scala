package com.ubirch.avatar.core.device

import com.ubirch.avatar.core.test.util.DeviceTypeTestUtil
import com.ubirch.avatar.test.base.ElasticsearchSpec
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.util.json.MyJsonProtocol

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-11-10
  */
class DeviceTypeManagerSpec extends ElasticsearchSpec
  with MyJsonProtocol {

  feature("all()") {

    scenario("index does not exist --> empty response") {
      deleteIndexes()
      runAllReturnsEmpty()
    }

    scenario("index exists; no records exist --> empty response") {
      runAllReturnsEmpty()
    }

    scenario("some records exist") {

      // prepare
      val dataSeries = DeviceTypeTestUtil.storeSeries()

      // test
      val all = Await.result(DeviceTypeManager.all(), 2 seconds)

      // verify
      all.size should be(dataSeries.size)
      all foreach dataSeries.contains

    }

  }

  feature("getByKey()") {

    scenario("index does not exist --> result is None") {
      deleteIndexes()
      runGetByKeyResultsInNone()
    }

    scenario("index exists; no record matching the given key exist --> result is None") {
      runGetByKeyResultsInNone()
    }

    scenario("record matching the given key exists") {
      val deviceType = DeviceTypeTestUtil.storeSeries(elementCount = 1).head
      Await.result(DeviceTypeManager.getByKey(deviceType.key), 1 second) should be(Some(deviceType))
    }

  }

  feature("create()") {

    scenario("index does not exist --> create is successful") {
      deleteIndexes()
      runCreateSuccessful()
    }

    scenario("index exists; no record with given key exists --> create is successful") {
      runCreateSuccessful()
    }

    scenario("record with given key exists --> create fails") {

      // prepare
      val deviceType = DeviceTypeUtil.defaultDeviceType()
      Await.result(DeviceTypeManager.create(deviceType), 1 second)
      Thread.sleep(2000)
      Await.result(DeviceTypeManager.all(), 1 second) should be(Seq(deviceType))

      // test
      val result = Await.result(DeviceTypeManager.create(deviceType), 1 second)
      Thread.sleep(1000)

      // verify
      result should be(None)
      Await.result(DeviceTypeManager.all(), 1 second) should be(Seq(deviceType))

    }

  }

  feature("update()") {

    scenario("index does not exist --> update fails") {
      deleteIndexes()
      runUpdateFails()
    }

    scenario("index exists; no record with given key exists --> update fails") {
      runUpdateFails()
    }

    scenario("record with given key exists --> update is successful") {

      // prepare
      val deviceType = DeviceTypeTestUtil.storeSeries(elementCount = 1).head
      Await.result(DeviceTypeManager.all(), 1 second) should be(Seq(deviceType))

      val updatedDeviceType = deviceType.copy(icon = s"${deviceType.icon}-2")

      // test
      val result = Await.result(DeviceTypeManager.update(updatedDeviceType), 1 second)
      Thread.sleep(1000)

      // verify
      result should be(Some(updatedDeviceType))
      Await.result(DeviceTypeManager.all(), 1 second) should be(Seq(updatedDeviceType))

    }

  }

  feature("init()") {

    scenario("index does not exist --> default deviceTypes are created") {
      deleteIndexes()
      runInitDeviceTypesAreCreated()
    }

    scenario("index exists; no records exist --> default deviceTypes are created") {
      runInitDeviceTypesAreCreated()
    }

    scenario("records exist --> no deviceTypes are created") {
      val deviceTypes = DeviceTypeTestUtil.storeSeries(prefix = "myDevice", elementCount = 2)
      Await.result(DeviceTypeManager.init(), 1 second) should be(deviceTypes)
    }

  }

  private def runAllReturnsEmpty() = {
    Await.result(DeviceTypeManager.all(), 1 second) should be('isEmpty)
  }

  private def runGetByKeyResultsInNone() = {
    Await.result(DeviceTypeManager.getByKey("unknownDevice"), 1 second) should be(None)
  }

  private def runCreateSuccessful() = {

    // (continue) prepare
    val deviceType = DeviceTypeUtil.defaultDeviceType()

    // test
    val result = Await.result(DeviceTypeManager.create(deviceType), 1 second)
    Thread.sleep(2000)

    // verify
    result should be(Some(deviceType))
    Await.result(DeviceTypeManager.all(), 1 second) should be(Seq(deviceType))

  }

  private def runUpdateFails() = {
    val defaultDeviceType = DeviceTypeUtil.defaultDeviceType()
    Await.result(DeviceTypeManager.update(defaultDeviceType), 1 second) should be(None)
  }

  private def runInitDeviceTypesAreCreated() = {
    val result = Await.result(DeviceTypeManager.init(), 1 second)
    result.toSet should be(DeviceTypeUtil.defaultDeviceTypes)
  }

}
