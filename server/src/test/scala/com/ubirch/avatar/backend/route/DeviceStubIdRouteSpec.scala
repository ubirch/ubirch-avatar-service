package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import com.ubirch.avatar.core.server.util.RouteConstants
import com.ubirch.avatar.model.{Device, DummyDevices}
import com.ubirch.avatar.test.base.RouteSpec
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceStubIdRouteSpec extends RouteSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.urlDevice}") {

    scenario("without deviceId") {
      Get(RouteConstants.urlDeviceStubWithId("")) ~> routes ~> check {
        handled shouldEqual false
      }
    }

    scenario("with deviceId") {

      val device = DummyDevices.device1
      val deviceId = device.deviceId

      Get(RouteConstants.urlDeviceStubWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Device] shouldEqual device
        verifyCORSHeader()
      }

    }

    // TODO test case: deviceId doesn't exist

  }

}
