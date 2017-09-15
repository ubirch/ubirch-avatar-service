package com.ubirch.avatar.core.device

import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.test.base.ElasticsearchSpecAsync
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.util.uuid.UUIDUtil

/**
  * author: cvandrei
  * since: 2017-07-10
  */
class DeviceManagerSpec extends ElasticsearchSpecAsync {

  feature("create()") {

    scenario("index does not exist; hwDeviceId does not exist --> succeed to create device") {

      // prepare
      deleteIndices()
      val device = DummyDevices.device()

      // test
      DeviceManager.create(device) flatMap {

        // verify
        case None => fail("failed to create device")

        case Some(created) =>

          val expected = DeviceUtil.deviceWithDefaults(device)
          created should be(expected)
          DeviceManager.infoByHwId(device.hwDeviceId) map (_ should be(None))

      }

    }

    scenario("index exists; hwDeviceId (lower case) does not exist --> succeed to create device (w/ hwDeviceId as lower case)") {

      // prepare
      val hwDeviceId = UUIDUtil.uuidStr.toLowerCase
      val device = DummyDevices.device(hwDeviceId = hwDeviceId)

      // test
      DeviceManager.create(device) flatMap {

        // verify
        case None => fail("failed to create device")

        case Some(created) =>

          val expected = DeviceUtil.deviceWithDefaults(device)
          created should be(expected)

          Thread.sleep(2000)
          DeviceManager.info(device.deviceId) map (_ should be(Some(expected)))
          DeviceManager.infoByHwId(device.hwDeviceId) map (_ should be(Some(expected)))

      }

    }

    scenario("index exists; hwDeviceId (upper case) does not exist --> succeed to create device (w/ hwDeviceId as lower case)") {

      // prepare
      val hwDeviceId = UUIDUtil.uuidStr.toUpperCase
      val device = DummyDevices.device(hwDeviceId = hwDeviceId)

      // test
      DeviceManager.create(device) flatMap {

        // verify
        case None => fail("failed to create device")

        case Some(created) =>

          val expected = DeviceUtil.deviceWithDefaults(device).copy(hwDeviceId = hwDeviceId.toLowerCase)
          created should be(expected)

          Thread.sleep(2000)
          DeviceManager.info(device.deviceId) map (_ should be(Some(expected)))
          DeviceManager.infoByHwId(device.hwDeviceId) map (_ should be(Some(expected)))

      }

    }

    scenario("index exists; hwDeviceId exists --> fails to create device") {

      // prepare
      val device = DummyDevices.device()

      DeviceManager.create(device) flatMap {

        case None => fail("failed to create device during preparation")

        case Some(prepared) =>

          val expected = DeviceUtil.deviceWithDefaults(device)
          prepared should be(expected)
          Thread.sleep(2000)
          DeviceManager.infoByHwId(device.hwDeviceId) map (_ should be(Some(expected)))

          // test && verify
          DeviceManager.create(device) map(_ should be(None))

      }

    }

  }

}
