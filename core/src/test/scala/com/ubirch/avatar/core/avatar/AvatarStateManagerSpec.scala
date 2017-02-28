package com.ubirch.avatar.core.avatar

import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.aws.AvatarState
import com.ubirch.avatar.test.base.ElasticsearchSpecAsync
import com.ubirch.util.uuid.UUIDUtil

import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2017-02-27
  */
class AvatarStateManagerSpec extends ElasticsearchSpecAsync {

  feature("byDeviceId()") {

    scenario("index does not exist") {
      deleteIndexes()
      AvatarStateManager.byDeviceId(UUIDUtil.uuid) map (_ should be(None))
    }

    scenario("index exists; record does not") {
      AvatarStateManager.byDeviceId(UUIDUtil.uuid) map (_ should be(None))
    }

    scenario("deviceId exists") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      AvatarStateManager.create(avatarState) flatMap { created =>

        Thread.sleep(1500)

        // test && verify
        AvatarStateManager.byDeviceId(deviceId) map { result =>
          result should be(created)
        }

      }

    }

  }

  feature("create") {

    scenario("index does not exist -> create is successful (and record can be found)") {

      // prepare
      deleteIndexes()
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      // test
      AvatarStateManager.create(avatarState) flatMap { result =>

        // verify
        result should be(Some(avatarState))

        Thread.sleep(1300)
        AvatarStateManager.byDeviceId(deviceId) map (_ should be(result))

      }

    }

    scenario("index exists -> create is successful") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      // test
      AvatarStateManager.create(avatarState) flatMap { result =>

        // verify
        result should be(Some(avatarState))

        Thread.sleep(1300)
        AvatarStateManager.byDeviceId(deviceId) map (_ should be(Some(avatarState)))

      }


    }

    scenario("record with same deviceId exists -> create fails") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      AvatarStateManager.create(avatarState) flatMap { prepareResult =>

        prepareResult should be(Some(avatarState))
        Thread.sleep(1300)

        // test && verify
        AvatarStateManager.create(avatarState) map (_ should be(None))

      }

    }

  }

  feature("update()") {

    scenario("index does not exist -> update fails") {

      // prepare
      deleteIndexes()
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      // test
      AvatarStateManager.update(avatarState) flatMap { result =>

        // verify
        result should be(None)

        Thread.sleep(2000)
        AvatarStateManager.byDeviceId(deviceId) map (_ should be(None))

      }

    }

    scenario("index exists; record does not -> update fails") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      // test
      AvatarStateManager.update(avatarState) flatMap { result =>

        // verify
        result should be(None)

        Thread.sleep(2000)
        AvatarStateManager.byDeviceId(deviceId) map (_ should be(None))

      }

    }

    scenario("record exists -> update succeeds") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)
      AvatarStateManager.create(avatarState) flatMap { createdOpt =>

        val created = createdOpt.get
        Thread.sleep(1500)
        val forUpdate = created.copy(avatarLastUpdated = Some(created.avatarLastUpdated.get.plusDays(1)))

        // test
        AvatarStateManager.update(forUpdate) flatMap { result =>

          // verify
          result should be(Some(forUpdate))

          Thread.sleep(3000)
          AvatarStateManager.byDeviceId(deviceId) map (_ should be(result))

        }


      }

    }

  }

  feature("upsert()") {

    scenario("index does not exist -> upsert succeeds and we can find the record afterwards") {

      // prepare
      deleteIndexes()
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      // test
      AvatarStateManager.upsert(avatarState) flatMap { result =>

        // verify
        result should be(Some(avatarState))

        Thread.sleep(2000)
        AvatarStateManager.byDeviceId(deviceId) map (_ should be(Some(avatarState)))

      }

    }

    scenario("index exists; record does not -> upsert succeeds") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      // test
      AvatarStateManager.upsert(avatarState) flatMap { result =>

        // verify
        result should be(Some(avatarState))

        Thread.sleep(2000)
        AvatarStateManager.byDeviceId(deviceId) map (_ should be(Some(avatarState)))

      }

    }

    scenario("record exists -> upsert succeeds") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)
      AvatarStateManager.upsert(avatarState) flatMap { initialUpsertOpt =>

        val initialUpsert = initialUpsertOpt.get
        initialUpsert should be(avatarState)
        val toUpdate = initialUpsert.copy(avatarLastUpdated = Some(initialUpsert.avatarLastUpdated.get.plusDays(1)))

        // test
        AvatarStateManager.upsert(toUpdate) flatMap { result =>

          // verify
          result should be(Some(toUpdate))

          Thread.sleep(2000)
          AvatarStateManager.byDeviceId(deviceId) map (_ should be(Some(toUpdate)))

        }

      }

    }

  }

}
