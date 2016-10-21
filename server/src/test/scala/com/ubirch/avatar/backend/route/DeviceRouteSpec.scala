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
class DeviceRouteSpec extends RouteSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.urlDevice}") {

    scenario("call") {
      Get(RouteConstants.urlDevice) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        val deviceList = responseAs[Seq[Device]]
        deviceList should be('nonEmpty)
        verifyCORSHeader()
      }
    }

  }

  feature(s"POST ${RouteConstants.urlDevice}") {

    scenario("with device json") {

      val deviceInput = DummyDevices.device1

      Post(RouteConstants.urlDevice, deviceInput) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Device] should be(deviceInput)
        verifyCORSHeader()
      }
    }

    scenario("without device json") {

      Post(RouteConstants.urlDevice) ~> routes ~> check {

        // TODO finalize test
        status shouldEqual BadRequest
        responseEntity.contentType should be(`application/json`)

        verifyCORSHeader()

      }
    }

    // TODO test case: fail to create device

  }

}
