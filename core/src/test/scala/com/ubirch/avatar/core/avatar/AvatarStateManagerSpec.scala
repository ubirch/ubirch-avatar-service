package com.ubirch.avatar.core.avatar

import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.aws.AvatarState
import com.ubirch.avatar.test.base.ElasticsearchSpec
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2017-02-27
  */
class AvatarStateManagerSpec extends ElasticsearchSpec {

  feature("byDeviceId()") {

    scenario("index does not exist") {
      deleteIndexes()
      Await.result(AvatarStateManager.byDeviceId(UUIDUtil.uuid), 1 second) should be(None)
    }

    scenario("index exists; record does not") {
      Await.result(AvatarStateManager.byDeviceId(UUIDUtil.uuid), 1 seconds) should be(None)
    }

    scenario("deviceId exists") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)
      val created = Await.result(AvatarStateManager.create(avatarState), 1 second)
      Thread.sleep(1500)

      // test && verify
      Await.result(AvatarStateManager.byDeviceId(deviceId), 2 second) should be(created)

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
      val result = Await.result(AvatarStateManager.create(avatarState), 1 second)

      // verify
      result should be(Some(avatarState))

      Thread.sleep(1300)
      Await.result(AvatarStateManager.byDeviceId(deviceId), 2 second) should be(result)

    }

    scenario("index exists -> create is successful") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      // test
      val result = Await.result(AvatarStateManager.create(avatarState), 2 second)

      // verify
      result should be(Some(avatarState))

      Thread.sleep(1300)
      Await.result(AvatarStateManager.byDeviceId(deviceId), 1 second) should be(Some(avatarState))

    }

    scenario("record with same deviceId exists -> create fails") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      val prepareResult = Await.result(AvatarStateManager.create(avatarState), 1 second)
      prepareResult should be(Some(avatarState))
      Thread.sleep(1300)

      // test && verify
      Await.result(AvatarStateManager.create(avatarState), 1 second) should be(None)

    }

  }

}
