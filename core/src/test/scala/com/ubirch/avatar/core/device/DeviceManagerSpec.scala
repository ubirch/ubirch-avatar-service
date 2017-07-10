package com.ubirch.avatar.core.device

import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.test.base.ElasticsearchSpecAsync
import com.ubirch.avatar.util.model.DeviceUtil

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

    scenario("index exists; hwDeviceId does not exist --> succeed to create device") {

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

      }

    }

    scenario("index exists; hwDeviceId exists --> fails to create device") {

      // prepare
      deleteIndices()
      val device = DummyDevices.device()

      DeviceManager.create(device) flatMap {

        // verify
        case None => fail("failed to create device during preparation")

        case Some(prepared) =>

          val expected = DeviceUtil.deviceWithDefaults(device)
          prepared should be(expected)
          Thread.sleep(5000)
          //DeviceManager.infoByHwId(device.hwDeviceId) map (_ should be(Some(expected)))

          // test && verify
          // TODO fix test
          DeviceManager.create(device) map(_ should be(None))

      }

    }

  }

}
