package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.test.base.RouteSpec
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.JsonErrorResponse
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.scalatest.{BeforeAndAfterAll, Matchers}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceIdRouteSpec extends RouteSpec
  with Matchers
  with BeforeAndAfterAll
  with ResponseUtil {


  //Ignored, because it's only being used by Trackle's deviceStats reparation request, which shouldn't be used
  // at the moment and will get removed soon.
  ignore(s"GET ${RouteConstants.pathDevice}/:deviceId") {

    val mainRoute = new MainRoute
    val routes = mainRoute.myRoute
    scenario("without deviceId") {
      Get(RouteConstants.pathDeviceWithId("")) ~> routes ~> check {
        handled shouldEqual false
      }
    }

    scenario("deviceId exists") {

      val device = DummyDevices.device1
      val deviceId = device.deviceId

      Get(RouteConstants.pathDeviceWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Device] shouldEqual device
        verifyCORSHeader()
      }

    }

    scenario("deviceId does not exists") {

      val deviceId = DummyDevices.device1.deviceId + "-does-not-exist"

      Get(RouteConstants.pathDeviceWithId(deviceId)) ~> routes ~> check {

        val s = status
        val r = response
        s shouldEqual BadRequest

        val expectedError = requestErrorResponse("QueryError", s"deviceId not found: deviceId=$deviceId")
        responseEntity.contentType should be(`application/json`)
        responseAs[JsonErrorResponse] shouldEqual expectedError

        verifyCORSHeader()

      }

    }

  }

  //Ignored, because it's only being used by the TrackleService when updating EOL flag in device config; this shall become refactored soon.
  ignore(s"PUT ${RouteConstants.pathDevice}/:deviceId") {

    val mainRoute = new MainRoute
    val routes = mainRoute.myRoute
    scenario("without deviceId") {
      Put(RouteConstants.pathDeviceWithId("")) ~> routes ~> check {
        handled shouldEqual false
      }
    }

    scenario("deviceId exists") {

      val device = DummyDevices.device1
      val deviceId = device.deviceId

      Put(RouteConstants.pathDeviceWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Device] shouldEqual device
        verifyCORSHeader()
      }

    }

    scenario("deviceId does not exist") {

      val deviceId = DummyDevices.device1.deviceId + "-does-not-exist"

      Put(RouteConstants.pathDeviceWithId(deviceId)) ~> routes ~> check {

        status shouldEqual BadRequest

        val expectedError = requestErrorResponse("UpdateError", s"failed to update device: deviceId=$deviceId")
        responseEntity.contentType should be(`application/json`)
        responseAs[JsonErrorResponse] shouldEqual expectedError

        verifyCORSHeader()

      }

    }

  }

  //Ignored, because it's only being used by User Delete endpoint in TrackleService that is not being used at the moment.
  ignore(s"DELETE ${RouteConstants.pathDevice}/:deviceId") {
    val mainRoute = new MainRoute
    val routes = mainRoute.myRoute
    scenario("without deviceId") {
      Delete(RouteConstants.pathDeviceWithId("")) ~> routes ~> check {
        handled shouldEqual false
      }
    }

    scenario("deviceId exists") {

      val device = DummyDevices.device1
      val deviceId = device.deviceId

      Delete(RouteConstants.pathDeviceWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Device] shouldEqual device
        verifyCORSHeader()
      }

    }

    scenario("deviceId does not exist") {

      val deviceId = DummyDevices.device1.deviceId + "-does-not-exist"

      Delete(RouteConstants.pathDeviceWithId(deviceId)) ~> routes ~> check {

        status shouldEqual BadRequest

        val expectedError = requestErrorResponse("DeleteError", s"failed to delete device: deviceId=$deviceId")
        responseEntity.contentType should be(`application/json`)
        responseAs[JsonErrorResponse] shouldEqual expectedError

        verifyCORSHeader()

      }

    }

  }

  override protected def beforeAll(): Unit = {
    DeviceManager.create(DummyDevices.device1)
  }
}
