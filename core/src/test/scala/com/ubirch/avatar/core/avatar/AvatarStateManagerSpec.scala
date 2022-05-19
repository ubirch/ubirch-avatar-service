package com.ubirch.avatar.core.avatar

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.db.device.AvatarState
import com.ubirch.avatar.mongo.MongoSpec
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.native.JsonMethods._

import scala.concurrent.Future
import scala.language.{existentials, postfixOps}

/**
  * author: cvandrei
  * since: 2017-02-27
  */
class AvatarStateManagerSpec extends MongoSpec {

  private val collection = Config.mongoCollectionAvatarState

  Feature("byDeviceId()") {

    Scenario("deviceId does not exist") {
      AvatarStateManager.byDeviceId(UUIDUtil.uuidStr) map (_ should be(None))
      mongoTestUtils.countAll(collection) map (_ shouldBe 0)
    }

    Scenario("deviceId exists") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceId = device.deviceId
      val avatarState = AvatarState(deviceId = deviceId)

      AvatarStateManager.create(avatarState) flatMap { created =>

        // test
        AvatarStateManager.byDeviceId(deviceId) flatMap { result =>

          // verify
          created shouldBe result
          mongoTestUtils.countAll(collection) map (_ shouldBe 1)
        }

      }

    }

  }

  Feature("create") {

    Scenario("create is successful") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val avatarState = AvatarState(deviceId = device.deviceId)

      // test
      AvatarStateManager.create(avatarState) flatMap { result =>

        // verify
        result should be(Some(avatarState))
        AvatarStateManager.byDeviceId(device.deviceId) map (_ should be(Some(avatarState)))
        mongoTestUtils.countAll(collection) map (_ shouldBe 1)

      }

    }

    Scenario("record with same deviceId exists -> create fails") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val avatarState = AvatarState(deviceId = device.deviceId)

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

  Feature("update()") {

    Scenario("record does not -> update fails") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val avatarState = AvatarState(deviceId = device.deviceId)

      // test
      AvatarStateManager.update(avatarState) flatMap { result =>

        // verify
        result should be(None)
        AvatarStateManager.byDeviceId(device.deviceId) map (_ should be(None))
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }
    }

    Scenario("record exists -> update succeeds") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val avatarState = AvatarState(deviceId = device.deviceId)
      AvatarStateManager.create(avatarState) flatMap { createdOpt =>

        val created = createdOpt.get
        val forUpdate = created.copy(avatarLastUpdated = Some(created.avatarLastUpdated.get.plusDays(1)))

        // test
        AvatarStateManager.update(forUpdate) flatMap { result =>

          // verify
          result should be(Some(forUpdate))
          AvatarStateManager.byDeviceId(device.deviceId) map (_ should be(result))
          mongoTestUtils.countAll(collection) map (_ shouldBe 1)

        }
      }
    }

  }

  Feature("upsert()") {

    Scenario("record does not exist -> upsert succeeds") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val avatarState = AvatarState(deviceId = device.deviceId)

      // test
      AvatarStateManager.upsert(avatarState) flatMap { result =>

        // verify
        result should be(Some(avatarState))
        AvatarStateManager.byDeviceId(device.deviceId) map (_ should be(Some(avatarState)))
        mongoTestUtils.countAll(collection) map (_ shouldBe 1)

      }

    }

    Scenario("record exists -> upsert succeeds") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val avatarState = AvatarState(deviceId = device.deviceId)
      AvatarStateManager.upsert(avatarState) flatMap { initialUpsertOpt =>

        val initialUpsert = initialUpsertOpt.get
        initialUpsert should be(avatarState)
        val toUpdate = initialUpsert.copy(avatarLastUpdated = Some(initialUpsert.avatarLastUpdated.get.plusDays(1)))

        // test
        AvatarStateManager.upsert(toUpdate) flatMap { result =>

          // verify
          result should be(Some(toUpdate))
          AvatarStateManager.byDeviceId(device.deviceId) map (_ should be(Some(toUpdate)))
          mongoTestUtils.countAll(collection) map (_ shouldBe 1)

        }

      }

    }

  }

  Feature("setReported()") {

    Scenario("no AvatarState exists --> creates a new one") {

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

          state.deviceId should be(device.deviceId)
          state.desired should be(Some(deviceConfigString))
          state.reported should be(Some(Json4sUtil.jvalue2String(reported)))

      }

    }

    Scenario("AvatarState exists (reported and desired not empty) --> update existing one") {

      // prepare
      val deviceConfigString = """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig)
      val reported = parse("""{"i":1200}""")

      val state = AvatarState(
        deviceId = device.deviceId,
        desired = Some("""{"i":700}"""),
        reported = Some("""{"i":900}"""),
        deviceLastUpdated = Some(DateTime.now(DateTimeZone.UTC).minusHours(1)),
        avatarLastUpdated = Some(DateTime.now(DateTimeZone.UTC).minusHours(1))
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
              updatedState shouldBe expected
          }

      }

    }

  }

  Feature("setDesired()") {

    Scenario("no AvatarState exists --> creates a new one") {

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

          state.deviceId should be(device.deviceId)
          state.desired should be(Some(Json4sUtil.jvalue2String(desired)))
          state.reported should be(Some("""{}"""))

      }

    }

    Scenario("AvatarState exists (reported and desired not empty; desired updates an existing field and adds a new one) --> update existing one") {

      // prepare
      val deviceConfigString =
        """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig)
      val desired = parse("""{"i":1200,"foo":"bar"}""")
      val reported = Some("""{"i":900}""")

      val state = AvatarState(
        deviceId = device.deviceId,
        desired = Some("""{"i":700}"""),
        reported = reported,
        deviceLastUpdated = Some(DateTime.now(DateTimeZone.UTC).minusHours(1)),
        avatarLastUpdated = Some(DateTime.now(DateTimeZone.UTC).minusHours(1))
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
              updatedState shouldBe expected
          }

      }

    }

    Scenario("AvatarState exists (reported and desired not empty; desired updates an existing field but doesn't remove an existing one) --> update existing one") {

      // prepare
      val deviceConfigString =
        """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig)
      val desired = parse("""{"i":1200}""")
      val reported = Some("""{"i":900}""")

      val state = AvatarState(
        deviceId = device.deviceId,
        desired = Some("""{"i":700,"foo":"bar"}"""),
        reported = reported,
        deviceLastUpdated = Some(DateTime.now(DateTimeZone.UTC).minusHours(1)),
        avatarLastUpdated = Some(DateTime.now(DateTimeZone.UTC).minusHours(1))
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
              updatedState shouldBe expected
          }

      }

    }

  }

}
