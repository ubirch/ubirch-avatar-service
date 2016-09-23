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
class DeviceIdRouteSpec extends RouteSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.urlDeviceWithIdPrefix}/:deviceId") {

    scenario("without deviceId") {
      Get(RouteConstants.urlDeviceWithId("")) ~> routes ~> check {
        handled shouldEqual false
      }
    }

    scenario("with deviceId") {
      val deviceId = "232343"
      Get(RouteConstants.urlDeviceWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Welcome].message shouldEqual s"GET ${RouteConstants.urlDeviceWithId(deviceId)}"
        verifyCORSHeader()
      }
    }

  }

  feature(s"PUT ${RouteConstants.urlDeviceWithIdPrefix}/:deviceId") {

    scenario("without deviceId") {
      Put(RouteConstants.urlDeviceWithId("")) ~> routes ~> check {
        handled shouldEqual false
      }
    }

    scenario("with deviceId") {
      val deviceId = "232343"
      Put(RouteConstants.urlDeviceWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Welcome].message shouldEqual s"POST ${RouteConstants.urlDeviceWithId(deviceId)}"
        verifyCORSHeader()
      }
    }

  }

  feature(s"DELETE ${RouteConstants.urlDeviceWithIdPrefix}/:deviceId") {

    scenario("without deviceId") {
      Delete(RouteConstants.urlDeviceWithId("")) ~> routes ~> check {
        handled shouldEqual false
      }
    }

    scenario("with deviceId") {
      val deviceId = "232343"
      Delete(RouteConstants.urlDeviceWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Welcome].message shouldEqual s"DELETE ${RouteConstants.urlDeviceWithId(deviceId)}"
        verifyCORSHeader()
      }
    }

  }

}
