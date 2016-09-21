package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import com.ubirch.avatar.core.server.util.RouteConstants
import com.ubirch.avatar.model.Welcome
import com.ubirch.avatar.test.base.RouteSpec
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceStubIdRoute extends RouteSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.urlDevice}") {

    scenario("without deviceId") {
      Get(RouteConstants.urlDeviceStubWithId("")) ~> routes ~> check {
        status shouldEqual MethodNotAllowed
      }
    }

    scenario("with deviceId") {
      val deviceId = "232343"
      Get(RouteConstants.urlDeviceStubWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Welcome].message shouldEqual s"GET ${RouteConstants.urlDeviceStubWithId(deviceId)}"
      }
    }

  }

}
