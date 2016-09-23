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
class DeviceRouteSpec extends RouteSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.urlDevice}") {

    scenario("call") {
      Get(RouteConstants.urlDevice) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Welcome].message shouldEqual s"GET ${RouteConstants.urlDevice}"
        verifyCORSHeader()
      }
    }

  }

  feature(s"POST ${RouteConstants.urlDevice}") {

    scenario("call") {
      Post(RouteConstants.urlDevice) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Welcome].message shouldEqual s"POST ${RouteConstants.urlDevice}"
        verifyCORSHeader()
      }
    }

  }

}
