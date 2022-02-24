package com.ubirch.avatar.backend.route

import akka.http.javadsl.model.HttpHeader
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.test.base.RouteSpec
import com.ubirch.avatar.util.server.RouteConstants
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceRouteSpec extends RouteSpec {


  //Ignoring tests, as this endpoint will soon become removed as it's only being used by the TrackleService to update
  // the device config; this implementation is going to be changed soon.
  ignore(s"GET ${RouteConstants.pathDevice}") {
    val routes = (new MainRoute).myRoute
    scenario("call") {
      Get(RouteConstants.pathDevice).addHeader(HttpHeader.parse("Authorization", "local-dev::")) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        val deviceList = responseAs[Seq[Device]]
        deviceList.nonEmpty shouldBe true
        verifyCORSHeader()
      }
    }

  }

  //Ignoring tests, as this endpoint will soon become removed as it's only being used by the TrackleService CMD script
  // to create devices; Device handling will be removed from Trackle Proxy, so this endpoint will become removed soon.
  ignore(s"POST ${RouteConstants.pathDevice}") {
    val routes = (new MainRoute).myRoute
    scenario("with device json") {

      val deviceInput = DummyDevices.device1

      Post(RouteConstants.pathDevice, deviceInput) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Device] should be(deviceInput)
        verifyCORSHeader()
      }
    }

    scenario("without device json") {

      Post(RouteConstants.pathDevice) ~> Route.seal(routes) ~> check {

        status shouldEqual BadRequest
        responseEntity.contentType should be(`text/plain(UTF-8)`)

        verifyCORSHeader(exist = false)

      }
    }

  }
}
