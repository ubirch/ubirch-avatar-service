package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
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
      Get(RouteConstants.urlDeviceWithId("")) ~> Route.seal(routes) ~> check {
        status shouldEqual MethodNotAllowed
      }
    }

    scenario("with deviceId") {
      val deviceId = "1111222233334444555566667777888899990000aaaabbbbccccddddeeeeffff"
      Get(RouteConstants.urlDeviceWithId(deviceId)) ~> routes ~> check {
        status shouldEqual NotFound
        responseEntity.contentType should be(`application/json`)
        responseAs[Welcome].message shouldEqual s"GET ${RouteConstants.urlDeviceWithId(deviceId)}"
      }
    }

  }

  feature(s"POST ${RouteConstants.urlDeviceWithIdPrefix}/:deviceId") {

    scenario("without deviceId") {
      Post(RouteConstants.urlDeviceWithId("")) ~> Route.seal(routes) ~> check {
        status shouldEqual MethodNotAllowed
      }
    }

    scenario("with deviceId") {
      val deviceId = "1111222233334444555566667777888899990000aaaabbbbccccddddeeeeffff"
      Post(RouteConstants.urlDeviceWithId(deviceId)) ~> Route.seal(routes) ~> check {
        status shouldEqual NotFound
        responseEntity.contentType should be(`application/json`)
        responseAs[Welcome].message shouldEqual s"POST ${RouteConstants.urlDeviceWithId(deviceId)}"
      }
    }

  }

  feature(s"DELETE ${RouteConstants.urlDeviceWithIdPrefix}/:deviceId") {

    scenario("without deviceId") {
      Delete(RouteConstants.urlDeviceWithId("")) ~> Route.seal(routes) ~> check {
        status shouldEqual MethodNotAllowed
      }
    }

    scenario("with deviceId") {
      val deviceId = "1111222233334444555566667777888899990000aaaabbbbccccddddeeeeffff"
      Delete(RouteConstants.urlDeviceWithId(deviceId)) ~> Route.seal(routes) ~> check {
        status shouldEqual NotFound
        responseEntity.contentType should be(`application/json`)
        responseAs[Welcome].message shouldEqual s"DELETE ${RouteConstants.urlDeviceWithId(deviceId)}"
      }
    }

  }

}
