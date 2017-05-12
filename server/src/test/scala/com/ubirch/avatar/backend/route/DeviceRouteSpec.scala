package com.ubirch.avatar.backend.route

import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.rest.device.Device
import com.ubirch.avatar.util.server.RouteConstants

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import com.ubirch.avatar.test.base.RouteSpec

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceRouteSpec extends RouteSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.pathDevice}") {

    scenario("call") {
      Get(RouteConstants.pathDevice) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        val deviceList = responseAs[Seq[Device]]
        deviceList should be('nonEmpty)
        verifyCORSHeader()
      }
    }

  }

  feature(s"POST ${RouteConstants.pathDevice}") {

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

        verifyCORSHeader(false)

      }
    }

    // TODO test case: fail to create device

  }

}
