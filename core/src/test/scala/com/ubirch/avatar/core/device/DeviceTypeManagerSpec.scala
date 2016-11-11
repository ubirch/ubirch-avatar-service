package com.ubirch.avatar.core.device

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

    ignore("some records exist") {
      // TODO implement test
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

    ignore("record matching the given key exists") {
      // TODO implement test
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

    ignore("record with given key exists --> create fails") {

      // prepare
      val deviceType = DeviceTypeUtil.defaultDeviceType()
      Await.result(DeviceTypeManager.create(deviceType), 1 second)
      Thread.sleep(1000)
      Await.result(DeviceTypeManager.all(), 1 second) should be(Seq(deviceType))

      // test
      val result = Await.result(DeviceTypeManager.create(deviceType), 1 second)

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

    ignore("record with given key exists --> update is successful") {
      // TODO implement test
    }

  }

  feature("init()") {

    ignore("index does not exist --> default deviceTypes are created") {
      deleteIndexes()
      // TODO implement test
    }

    ignore("index exists; no records exist --> default deviceTypes are created") {
      // TODO implement test
    }

    ignore("records exist --> no deviceTypes are created") {
      // TODO implement test
    }

  }

}
