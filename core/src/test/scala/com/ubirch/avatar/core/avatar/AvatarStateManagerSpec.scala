package com.ubirch.avatar.core.avatar

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.db.device.AvatarState
import com.ubirch.avatar.mongo.MongoSpec
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.DateTime
import org.json4s.native.JsonMethods._

import scala.concurrent.Future
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2017-02-27
  */
class AvatarStateManagerSpec extends MongoSpec {

  private val collection = Config.mongoCollectionAvatarState

  feature("byDeviceId()") {

    scenario("deviceId does not exist") {
      AvatarStateManager.byDeviceId(UUIDUtil.uuid) map (_ should be(None))
      mongoTestUtils.countAll(collection) map (_ shouldBe 0)
    }

    scenario("deviceId exists") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      AvatarStateManager.create(avatarState) flatMap { created =>

        // test
        AvatarStateManager.byDeviceId(deviceId) flatMap { result =>

          // verify
          result should be(created)
          mongoTestUtils.countAll(collection) map (_ shouldBe 1)

        }

      }

    }

  }

  feature("create") {

    scenario("create is successful") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      // test
      AvatarStateManager.create(avatarState) flatMap { result =>

        // verify
        result should be(Some(avatarState))
        AvatarStateManager.byDeviceId(deviceId) map (_ should be(Some(avatarState)))
        mongoTestUtils.countAll(collection) map (_ shouldBe 1)

      }


    }

    scenario("record with same deviceId exists -> create fails") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      AvatarStateManager.create(avatarState) flatMap { prepareResult =>

        prepareResult should be(Some(avatarState))

        // test
        AvatarStateManager.create(avatarState) flatMap { result =>

          // verify
          result should be(None)
          mongoTestUtils.countAll(collection) map (_ shouldBe 1)

        }

      }

    }

  }

  feature("update()") {

    scenario("record does not -> update fails") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      // test
      AvatarStateManager.update(avatarState) flatMap { result =>

        // verify
        result should be(None)
        AvatarStateManager.byDeviceId(deviceId) map (_ should be(None))
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)


      }

    }

    scenario("record exists -> update succeeds") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)
      AvatarStateManager.create(avatarState) flatMap { createdOpt =>

        val created = createdOpt.get
        val forUpdate = created.copy(avatarLastUpdated = Some(created.avatarLastUpdated.get.plusDays(1)))

        // test
        AvatarStateManager.update(forUpdate) flatMap { result =>

          // verify
          result should be(Some(forUpdate))
          AvatarStateManager.byDeviceId(deviceId) map (_ should be(result))
          mongoTestUtils.countAll(collection) map (_ shouldBe 1)

        }


      }

    }

  }

  feature("upsert()") {

    scenario("record does not exist -> upsert succeeds") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = UUIDUtil.fromString(device.deviceId)
      val avatarState = AvatarState(deviceId = deviceId)

      // test
      AvatarStateManager.upsert(avatarState) flatMap { result =>

        // verify
        result should be(Some(avatarState))
        AvatarStateManager.byDeviceId(deviceId) map (_ should be(Some(avatarState)))
        mongoTestUtils.countAll(collection) map (_ shouldBe 1)

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
          AvatarStateManager.byDeviceId(deviceId) map (_ should be(Some(toUpdate)))
          mongoTestUtils.countAll(collection) map (_ shouldBe 1)

        }

      }

    }

  }

  feature("setReported()") {

    scenario("no AvatarState exists --> creates a new one") {

      // prepare
      val deviceConfigString = """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig)
      val reported = parse("""{"i":900}""")

      // test
      AvatarStateManager.setReported(device, reported) flatMap {

        // verify
        case None => Future(fail("failed to create avatar state"))

        case Some(state: AvatarState) =>

          mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          state.deviceId should be(UUIDUtil.fromString(device.deviceId))
          state.desired should be(Some(deviceConfigString))
          state.reported should be(Some(Json4sUtil.jvalue2String(reported)))

      }

    }

    scenario("AvatarState exists (reported and desired not empty) --> update existing one") {

      // prepare
      val deviceConfigString = """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig)
      val reported = parse("""{"i":1200}""")

      val state = AvatarState(
        deviceId = UUIDUtil.fromString(device.deviceId),
        desired = Some("""{"i":700}"""),
        reported = Some("""{"i":900}"""),
        deviceLastUpdated = Some(DateTime.now.minusHours(1)),
        avatarLastUpdated = Some(DateTime.now.minusHours(1))
      )
      AvatarStateManager.create(state) flatMap {

        case None => fail("failed to create existing avatar state")

        case Some(existingState: AvatarState) =>

          existingState should be(state)

          // test
          AvatarStateManager.setReported(device, reported) flatMap {

            // verify
            case None => Future(fail("failed to update avatar state"))

            case Some(updatedState: AvatarState) =>

              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

              val expected = state.copy(
                reported = Some(Json4sUtil.jvalue2String(reported)),
                deviceLastUpdated = updatedState.deviceLastUpdated
              )
              updatedState should be(expected)

          }

      }

    }

  }

  feature("setDesired()") {

    scenario("no AvatarState exists --> creates a new one") {

      // prepare
      val deviceConfigString =
        """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig)
      val desired = parse("""{"i":900}""")

      // test
      AvatarStateManager.setDesired(device, desired) flatMap {

        // verify
        case None => Future(fail("failed to create avatar state"))

        case Some(state: AvatarState) =>

          mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          state.deviceId should be(UUIDUtil.fromString(device.deviceId))
          state.desired should be(Some(Json4sUtil.jvalue2String(desired)))
          state.reported should be(Some("""{}"""))

      }

    }

    scenario("AvatarState exists (reported and desired not empty; desired updates an existing field and adds a new one) --> update existing one") {

      // prepare
      val deviceConfigString =
        """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig)
      val desired = parse("""{"i":1200,"foo":"bar"}""")
      val reported = Some("""{"i":900}""")

      val state = AvatarState(
        deviceId = UUIDUtil.fromString(device.deviceId),
        desired = Some("""{"i":700}"""),
        reported = reported,
        deviceLastUpdated = Some(DateTime.now.minusHours(1)),
        avatarLastUpdated = Some(DateTime.now.minusHours(1))
      )
      AvatarStateManager.create(state) flatMap {

        case None => fail("failed to prepare test")

        case Some(existingState: AvatarState) =>

          existingState should be(state)

          // test
          AvatarStateManager.setDesired(device, desired) flatMap {

            // verify
            case None => Future(fail("failed to update avatar state"))

            case Some(updatedState: AvatarState) =>

              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

              val expected = state.copy(
                desired = Some(Json4sUtil.jvalue2String(desired)),
                avatarLastUpdated = updatedState.avatarLastUpdated
              )
              updatedState should be(expected)

          }

      }

    }

    scenario("AvatarState exists (reported and desired not empty; desired updates an existing field but doesn't remove an existing one) --> update existing one") {

      // prepare
      val deviceConfigString =
        """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig)
      val desired = parse("""{"i":1200}""")
      val reported = Some("""{"i":900}""")

      val state = AvatarState(
        deviceId = UUIDUtil.fromString(device.deviceId),
        desired = Some("""{"i":700,"foo":"bar"}"""),
        reported = reported,
        deviceLastUpdated = Some(DateTime.now.minusHours(1)),
        avatarLastUpdated = Some(DateTime.now.minusHours(1))
      )
      AvatarStateManager.create(state) flatMap {

        case None => fail("failed to prepare test")

        case Some(existingState: AvatarState) =>

          existingState should be(state)

          // test
          AvatarStateManager.setDesired(device, desired) flatMap {

            // verify
            case None => Future(fail("failed to update avatar state"))

            case Some(updatedState: AvatarState) =>

              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

              val expected = state.copy(
                desired = Some("""{"i":1200,"foo":"bar"}"""),
                avatarLastUpdated = updatedState.avatarLastUpdated
              )
              updatedState should be(expected)

          }

      }

    }

  }

}
