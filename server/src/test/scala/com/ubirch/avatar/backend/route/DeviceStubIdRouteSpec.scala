package com.ubirch.avatar.backend.route

import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.test.base.RouteSpec
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.JsonErrorResponse

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.util.mongo.connection.MongoUtil

import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import play.api.libs.ws.WSClient
import play.api.libs.ws.ning.NingWSClient

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceStubIdRouteSpec extends RouteSpec
  with ResponseUtil {

  implicit val ws: WSClient = NingWSClient()
  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.pathDevice}") {

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
