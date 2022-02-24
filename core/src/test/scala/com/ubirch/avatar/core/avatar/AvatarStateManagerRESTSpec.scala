package com.ubirch.avatar.core.avatar

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.AvatarState
import com.ubirch.avatar.mongo.MongoSpec
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.native.JsonMethods._

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-15
  */
class AvatarStateManagerRESTSpec extends MongoSpec {

  private val collection = Config.mongoCollectionAvatarState
  private val emptyJson = Some(parse("{}"))

  feature("setReported()") {

    scenario("no AvatarState exists --> creates a new one") {

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

    scenario("AvatarState exists (reported and desired not empty) --> update existing one") {

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

  feature("setDesired()") {

    scenario("no AvatarState exists --> creates a new one") {

      // prepare
      val deviceConfigString =
        """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = Json4sUtil.any2any[Device](DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig))
      val desired = parse("""{"i":900}""")

      // test
      AvatarStateManagerREST.setDesired(device, desired) flatMap {

        // verify
        case None => Future(fail("failed to create avatar state"))

        case Some(state: AvatarState) =>

          mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          state.deviceId should be(device.deviceId)
          state.inSync should be(Some(false))
          state.desired should be(Some(desired))
          state.reported should be(Some(parse("""{}""")))
          state.delta should be(Some(desired))

      }

    }

    scenario("AvatarState exists (reported and desired not empty; desired updates an existing field and adds a new one) --> update existing one") {

      // prepare
      val deviceConfigString =
        """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = Json4sUtil.any2any[Device](DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig))
      val desired = parse("""{"i":1200,"foo":"bar"}""")
      val reported = parse("""{"i":900}""")

      val state = db.device.AvatarState(
        deviceId = device.deviceId,
        desired = Some(Json4sUtil.jvalue2String(desired)),
        reported = Some(Json4sUtil.jvalue2String(reported)),
        deviceLastUpdated = Some(DateTime.now(DateTimeZone.UTC).minusHours(1)),
        avatarLastUpdated = Some(DateTime.now(DateTimeZone.UTC).minusHours(1))
      )
      AvatarStateManager.create(state) flatMap {

        case None => fail("failed to create existing avatar state")

        case Some(existingState: db.device.AvatarState) =>

          existingState should be(state)

          // test
          AvatarStateManagerREST.setDesired(device, desired) flatMap {

            // verify
            case None => Future(fail("failed to update avatar state"))

            case Some(state: AvatarState) =>

              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

              val expectedDesiredAndDelta = Some(desired)

              state.deviceId should be(device.deviceId)
              state.inSync should be(Some(false))
              state.desired should be(expectedDesiredAndDelta)
              state.reported.get should be(reported)
              state.delta should be(expectedDesiredAndDelta)
              state.deviceLastUpdated should be(existingState.deviceLastUpdated)
              state.avatarLastUpdated.get.isAfter(existingState.avatarLastUpdated.get) should be(true)

          }

      }

    }

    scenario("AvatarState exists (reported and desired not empty; desired updates an existing field but doesn't remove an existing one) --> update existing one") {

      // prepare
      val deviceConfigString =
        """{"i":600}"""
      val deviceConfig = Some(parse(deviceConfigString))
      val device = Json4sUtil.any2any[Device](DummyDevices.minimalDevice().copy(deviceConfig = deviceConfig))
      val desiredOld = parse("""{"i":700,"foo":"bar"}""")
      val desiredNew = parse("""{"i":1200}""")
      val reported = parse("""{"i":900}""")

      val state = db.device.AvatarState(
        deviceId = device.deviceId,
        desired = Some(Json4sUtil.jvalue2String(desiredOld)),
        reported = Some(Json4sUtil.jvalue2String(reported)),
        deviceLastUpdated = Some(DateTime.now(DateTimeZone.UTC).minusHours(1)),
        avatarLastUpdated = Some(DateTime.now(DateTimeZone.UTC).minusHours(1))
      )
      AvatarStateManager.create(state) flatMap {

        case None => fail("failed to create existing avatar state")

        case Some(existingState: db.device.AvatarState) =>

          existingState should be(state)

          // test
          AvatarStateManagerREST.setDesired(device, desiredNew) flatMap {

            // verify
            case None => Future(fail("failed to update avatar state"))

            case Some(state: AvatarState) =>

              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

              val expectedDesiredAndDelta = Some(parse("""{"i":1200,"foo":"bar"}"""))

              state.deviceId should be(device.deviceId)
              state.inSync should be(Some(false))
              state.desired should be(expectedDesiredAndDelta)
              state.reported.get should be(reported)
              state.delta should be(expectedDesiredAndDelta)
              state.deviceLastUpdated should be(existingState.deviceLastUpdated)
              state.avatarLastUpdated.get.isAfter(existingState.avatarLastUpdated.get) should be(true)

          }

      }

    }

  }

  feature("toRestModel()") {

    scenario("_desired_ and _reported_ are empty") {

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

    scenario("_desired_ and _reported_ have same jsons") {

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

    scenario("_desired_ and _reported_ have same field with different values") {

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

    scenario("_desired_ has a field that _reported_ does not") {

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

    scenario("_reported_ has a field that _desired_ does not") {

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
