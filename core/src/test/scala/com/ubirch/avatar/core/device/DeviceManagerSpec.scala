package com.ubirch.avatar.core.device

import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.test.base.ElasticsearchSpecAsync
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.{DateTime, DateTimeZone}

/**
  * author: cvandrei
  * since: 2017-07-10
  */
class DeviceManagerSpec extends ElasticsearchSpecAsync {

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  feature("create()") {

    scenario("index does not exist; empty database --> succeed to create device") {

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

          val deviceToCreate = device.copy(deviceId = UUIDUtil.uuidStr)

          // test && verify
          DeviceManager.create(deviceToCreate) map(_ should be(None))

      }

    }

    scenario("index exists; deviceId exists --> fails to create device") {

      // prepare
      val device = DummyDevices.device()

      DeviceManager.create(device) flatMap {

        case None => fail("failed to create device during preparation")

        case Some(prepared) =>

          val expected = DeviceUtil.deviceWithDefaults(device)
          prepared should be(expected)
          Thread.sleep(2000)
          DeviceManager.infoByHwId(device.hwDeviceId) map (_ should be(Some(expected)))

          val deviceToCreate = device.copy(hwDeviceId = UUIDUtil.uuidStr.toLowerCase)

          // test && verify
          DeviceManager.create(deviceToCreate) map(_ should be(None))

      }

    }

  }

  feature("update()") {

    scenario("index does not exist; empty database --> update fails") {

      // prepare
      deleteIndices()
      val device = DummyDevices.device()

      // test && verify
      DeviceManager.update(device) map(_ should be(None))

    }

    scenario("index exists; empty database --> update fails") {

      // prepare
      val device = DummyDevices.device()

      // test && verify
      DeviceManager.update(device) map(_ should be(None))

    }

    scenario("index exists; device exists; no changes --> update succeeds") {

      // prepare
      DeviceManager.create(DummyDevices.device()) flatMap {

        case None => fail("failed to prepare device")

        case Some(device) =>

          // test
          DeviceManager.update(device) flatMap { updated =>

            // verify
            Thread.sleep(2000)
            val expected = DeviceUtil.deviceWithDefaults(device)
            updated should be(Some(expected))

          }

      }

    }

    scenario("index exists; device exists; changing the deviceId --> update fails") {

      // prepare
      DeviceManager.create(DummyDevices.device()) flatMap {

        case None => fail("failed to prepare device")

        case Some(device) =>

          Thread.sleep(2000)
          val toUpdate = device.copy(deviceId = UUIDUtil.uuidStr)

          // test && verify
          DeviceManager.update(toUpdate) map(_ should be(None))

      }

    }

    scenario("index exists; device exists; changing the hwDeviceId--> update fails") {

      // prepare
      DeviceManager.create(DummyDevices.device()) flatMap {

        case None => fail("failed to prepare device")

        case Some(device) =>

          Thread.sleep(2000)
          val toUpdate = device.copy(hwDeviceId = UUIDUtil.uuidStr.toLowerCase())

          // test && verify
          DeviceManager.update(toUpdate) map(_ should be(None))

      }

    }

    scenario("index exists; device exists; changing the hashedHwDeviceId--> update fails") {

      // prepare
      DeviceManager.create(DummyDevices.device()) flatMap {

        case None => fail("failed to prepare device")

        case Some(device) =>

          Thread.sleep(2000)
          val newHashedHwId = HashUtil.sha512Base64(UUIDUtil.uuidStr.toLowerCase())
          val toUpdate = device.copy(hashedHwDeviceId = newHashedHwId)

          // test && verify
          DeviceManager.update(toUpdate) map(_ should be(None))

      }

    }

    scenario("index exists; device exists; changing created--> update fails") {

      // prepare
      DeviceManager.create(DummyDevices.device()) flatMap {

        case None => fail("failed to prepare device")

        case Some(device) =>

          Thread.sleep(2000)
          val toUpdate = device.copy(created = DateTime.now(DateTimeZone.UTC).plusYears(10))

          // test && verify
          DeviceManager.update(toUpdate) map(_ should be(None))

      }

    }

    scenario("index exists; device exists; changing owners--> update succeeds") {

      // prepare
      DeviceManager.create(DummyDevices.device()) flatMap {

        case None => fail("failed to prepare device")

        case Some(device) =>

          Thread.sleep(2000)
          val toUpdate = device.copy(owners = device.owners + UUIDUtil.uuid)

          // test && verify
          DeviceManager.update(toUpdate) flatMap { updated =>

            Thread.sleep(2000)
            val expected = DeviceUtil.deviceWithDefaults(toUpdate)
            updated should be(Some(expected))

          }

      }

    }

  }

}
