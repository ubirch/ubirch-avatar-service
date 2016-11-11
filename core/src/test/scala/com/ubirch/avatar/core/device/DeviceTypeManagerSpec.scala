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
      Await.result(DeviceTypeManager.all(), 1 second) should be('isEmpty)
    }

    scenario("index exists; no records exist --> empty response") {
      Await.result(DeviceTypeManager.all(), 1 second) should be('isEmpty)
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
      Await.result(DeviceTypeManager.getByKey("unknownDevice"), 1 second) should be(None)
    }

    scenario("index exists; no record matching the given key exist --> result is None") {
      Await.result(DeviceTypeManager.getByKey("unknownDevice"), 1 second) should be(None)
    }

    scenario("record matching the given key exists") {

      // prepare
      val deviceType = DeviceTypeTestUtil.storeSeries(elementCount = 1).head

      // test & verify
      Await.result(DeviceTypeManager.getByKey(deviceType.key), 1 second) should be(Some(deviceType))

    }

  }

  feature("create()") {

    scenario("index does not exist --> create is successful") {

      // prepare
      deleteIndexes()
      val deviceType = DeviceTypeUtil.defaultDeviceType()

      // test
      val result = Await.result(DeviceTypeManager.create(deviceType), 1 second)
      Thread.sleep(2000)

      // verify
      result should be(Some(deviceType))
      Await.result(DeviceTypeManager.all(), 1 second) should be(Seq(deviceType))

    }

    scenario("index exists; no record with given key exists --> create is successful") {

      // prepare
      deleteIndexes()
      val deviceType = DeviceTypeUtil.defaultDeviceType()

      // test
      val result = Await.result(DeviceTypeManager.create(deviceType), 1 second)
      Thread.sleep(2000)

      // verify
      result should be(Some(deviceType))
      Await.result(DeviceTypeManager.all(), 1 second) should be(Seq(deviceType))

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
      val defaultDeviceType = DeviceTypeUtil.defaultDeviceType()
      Await.result(DeviceTypeManager.update(defaultDeviceType), 1 second) should be(None)
    }

    scenario("index exists; no record with given key exists --> update fails") {
      val defaultDeviceType = DeviceTypeUtil.defaultDeviceType()
      Await.result(DeviceTypeManager.update(defaultDeviceType), 1 second) should be(None)
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

      // prepare
      deleteIndexes()

      // test
      val result = Await.result(DeviceTypeManager.init(), 1 second)

      // verify
      result.toSet should be(DeviceTypeUtil.defaultDeviceTypes)

    }

    scenario("index exists; no records exist --> default deviceTypes are created") {
      val result = Await.result(DeviceTypeManager.init(), 1 second)
      result.toSet should be(DeviceTypeUtil.defaultDeviceTypes)
    }

    scenario("records exist --> no deviceTypes are created") {

      // prepare
      val deviceTypes = DeviceTypeTestUtil.storeSeries(prefix = "myDevice", elementCount = 2)

      // test & verify
      Await.result(DeviceTypeManager.init(), 1 second) should be(deviceTypes)

    }

  }

}
