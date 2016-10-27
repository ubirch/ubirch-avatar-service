package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import com.ubirch.avatar.core.server.util.RouteConstants
import com.ubirch.avatar.model.{ErrorFactory, ErrorResponse}
import com.ubirch.avatar.test.base.{ElasticsearchSpec, RouteSpec}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-30
  */
class DeviceDataHistoryRouteSpec extends RouteSpec
  with ElasticsearchSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.urlDeviceHistory(":deviceId")}") {

    ignore("deviceId exists") {
      // TODO write test
    }

    scenario("deviceId does not exists") {

      val deviceId = "1234asdf"
      val from = None
      val size = None

      Get(RouteConstants.urlDeviceHistory(deviceId)) ~> routes ~> check {

        status shouldEqual BadRequest

        val expectedError = ErrorFactory.create("QueryError", s"deviceId not found: deviceId=$deviceId, from=$from, size=$size")
        responseEntity.contentType should be(`application/json`)
        responseAs[ErrorResponse] shouldEqual expectedError

        verifyCORSHeader()

      }
    }

  }

  feature(s"GET ${RouteConstants.urlDeviceHistory(":deviceId")}/:from") {

    ignore("deviceId exists") {
      // TODO write test
    }

    scenario("deviceId does not exists") {

      val deviceId = "1234asdf"
      val from = 5L
      val size = None

      Get(RouteConstants.urlDeviceHistoryFrom(deviceId, from)) ~> routes ~> check {

        status shouldEqual BadRequest

        val expectedError = ErrorFactory.create("QueryError", s"deviceId not found: deviceId=$deviceId, from=${Some(from)}, size=$size")
        responseEntity.contentType should be(`application/json`)
        responseAs[ErrorResponse] shouldEqual expectedError

        verifyCORSHeader()

      }
    }

  }

  feature(s"GET ${RouteConstants.urlDeviceHistory(":deviceId")}/:from/:size") {

    ignore("deviceId exists") {
      // TODO write test
    }

    scenario("deviceId does not exists") {

      val deviceId = "1234asdf"
      val from = 5L
      val size = 7L

      Get(RouteConstants.urlDeviceHistoryFromSize(deviceId, from, size)) ~> routes ~> check {

        status shouldEqual BadRequest

        val expectedError = ErrorFactory.create("QueryError", s"deviceId not found: deviceId=$deviceId, from=${Some(from)}, size=${Some(size)}")
        responseEntity.contentType should be(`application/json`)
        responseAs[ErrorResponse] shouldEqual expectedError

        verifyCORSHeader()

      }
    }

  }

}
