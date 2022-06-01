package com.ubirch.avatar.core.avatar

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.AvatarState
import com.ubirch.avatar.mongo.MongoSpec
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime
import org.json4s.native.JsonMethods._

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-15
  */
class AvatarStateManagerRESTSpec extends MongoSpec {

  private val collection = Config.mongoCollectionAvatarState
  private val emptyJson = Some(parse("{}"))

  Feature("setReported()") {

    Scenario("no AvatarState exists --> creates a new one") {

      // prepare
      val deviceConfigString =
        """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = Json4sUtil.any2any[Device](DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig))
      val reported = parse("""{"i":900}""")

      // test
      AvatarStateManagerREST.setReported(device, reported) flatMap {

        // verify
        case None => Future(fail("failed to create avatar state"))

        case Some(state: AvatarState) =>
          mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          state.deviceId should be(device.deviceId)
          state.inSync should be(Some(false))
          state.desired should be(deviceConfig)
          state.reported should be(Some(reported))
          state.delta should be(deviceConfig)

      }

    }

    Scenario("AvatarState exists (reported and desired not empty) --> update existing one") {

      // prepare
      val deviceConfigString =
        """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = Json4sUtil.any2any[Device](DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig))
      val reported = parse("""{"i":1200}""")

      val state = db.device.AvatarState(
        deviceId = device.deviceId,
        desired = Some("""{"i":700}"""),
        reported = Some("""{"i":900}"""),
        deviceLastUpdated = Some(DateTime.now.minusHours(1)),
        avatarLastUpdated = Some(DateTime.now.minusHours(1))
      )
      AvatarStateManager.create(state) flatMap {

        case None => fail("failed to create existing avatar state")

        case Some(existingState: db.device.AvatarState) =>
          existingState should be(state)

          // test
          AvatarStateManagerREST.setReported(device, reported) flatMap {

            // verify
            case None => Future(fail("failed to update avatar state"))

            case Some(state: AvatarState) =>
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

              val expectedDesiredAndDelta = Some(parse(existingState.desired.get))

              state.deviceId should be(device.deviceId)
              state.inSync should be(Some(false))
              state.desired should be(expectedDesiredAndDelta)
              state.reported should be(Some(reported))
              state.delta should be(expectedDesiredAndDelta)

          }

      }

    }

  }

  Feature("toRestModel()") {

    Scenario("_desired_ and _reported_ are empty") {

      // prepare
      val dbState = db.device.AvatarState(
        deviceId = UUIDUtil.uuidStr
      )

      // test
      val restState = AvatarStateManagerREST.toRestModel(dbState)

      // verify
      restState.deviceId should be(dbState.deviceId)
      restState.inSync should be(Some(true))
      restState.delta should be(emptyJson)

    }

    Scenario("_desired_ and _reported_ have same jsons") {

      // prepare
      val desired =
        """{"i":600}"""
      val reported = desired
      val deltaExpected = emptyJson
      val dbState = db.device.AvatarState(
        deviceId = UUIDUtil.uuidStr,
        desired = Some(desired),
        reported = Some(reported)
      )

      // test
      val restState = AvatarStateManagerREST.toRestModel(dbState)

      // verify
      restState.deviceId should be(dbState.deviceId)
      restState.inSync should be(Some(true))
      restState.desired should be(Some(parse(dbState.desired.get)))
      restState.reported should be(Some(parse(dbState.reported.get)))
      restState.delta should be(deltaExpected)

    }

    Scenario("_desired_ and _reported_ have same field with different values") {

      // prepare
      val desired =
        """{"i":900}"""
      val reported = """{"i":600}"""
      val deltaExpected = Some(parse(desired))
      val dbState = db.device.AvatarState(
        deviceId = UUIDUtil.uuidStr,
        desired = Some(desired),
        reported = Some(reported)
      )

      // test
      val restState = AvatarStateManagerREST.toRestModel(dbState)

      // verify
      restState.deviceId should be(dbState.deviceId)
      restState.inSync should be(Some(false))
      restState.desired should be(Some(parse(dbState.desired.get)))
      restState.reported should be(Some(parse(dbState.reported.get)))
      restState.delta should be(deltaExpected)

    }

    Scenario("_desired_ has a field that _reported_ does not") {

      // prepare
      val desired =
        """{"i":900,"foo":"bar"}"""
      val reported = """{"i":900}"""
      val deltaExpected = Some(parse("""{"foo":"bar"}"""))
      val dbState = db.device.AvatarState(
        deviceId = UUIDUtil.uuidStr,
        desired = Some(desired),
        reported = Some(reported)
      )

      // test
      val restState = AvatarStateManagerREST.toRestModel(dbState)

      // verify
      restState.deviceId should be(dbState.deviceId)
      restState.inSync should be(Some(false))
      restState.desired should be(Some(parse(dbState.desired.get)))
      restState.reported should be(Some(parse(dbState.reported.get)))
      restState.delta should be(deltaExpected)

    }

    Scenario("_reported_ has a field that _desired_ does not") {

      // prepare
      val desired =
        """{"i":900}"""
      val reported = """{"i":900,"foo":"bar"}"""
      val deltaExpected = emptyJson
      val dbState = db.device.AvatarState(
        deviceId = UUIDUtil.uuidStr,
        desired = Some(desired),
        reported = Some(reported)
      )

      // test
      val restState = AvatarStateManagerREST.toRestModel(dbState)

      // verify
      restState.deviceId should be(dbState.deviceId)
      restState.inSync should be(Some(false))
      restState.desired should be(Some(parse(dbState.desired.get)))
      restState.reported should be(Some(parse(dbState.reported.get)))
      restState.delta should be(deltaExpected)

    }

  }

}
