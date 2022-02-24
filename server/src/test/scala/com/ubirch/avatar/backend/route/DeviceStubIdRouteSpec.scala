package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.test.base.RouteSpec
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.JsonErrorResponse
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceStubIdRouteSpec extends RouteSpec
  with ResponseUtil {


  //route is not being used anymore; only by sleepingPeriod/<id> route in trackle service that is not being used anymore
  ignore(s"GET ${RouteConstants.pathDevice}") {
    val routes = (new MainRoute).myRoute
    scenario("without deviceId") {
      Get(RouteConstants.pathDeviceStubWithId("")) ~> routes ~> check {
        handled shouldEqual false
      }
    }

    scenario("deviceId exists") {

      val device = DummyDevices.device1
      val deviceId = device.deviceId

      Get(RouteConstants.pathDeviceStubWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Device] shouldEqual device
        verifyCORSHeader()
      }

    }

    scenario("deviceId does not exist") {

      val deviceId = DummyDevices.device1.deviceId + "-does-not-exist"

      Get(RouteConstants.pathDeviceStubWithId(deviceId)) ~> routes ~> check {

        status shouldEqual BadRequest

        val expectedError = requestErrorResponse("QueryError", s"deviceId not found: deviceId=$deviceId")
        responseEntity.contentType should be(`application/json`)
        responseAs[JsonErrorResponse] shouldEqual expectedError

        verifyCORSHeader()

      }

    }

  }

}
