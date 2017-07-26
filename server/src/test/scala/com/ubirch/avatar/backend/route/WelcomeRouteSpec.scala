package com.ubirch.avatar.backend.route

import com.ubirch.avatar.test.base.RouteSpec
import com.ubirch.util.model.JsonResponse

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-22
  */
class WelcomeRouteSpec extends RouteSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"call health/welcome page") {

    scenario("GET /") {
      Get() ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[JsonResponse] shouldEqual JsonResponse(message = "Welcome to the ubirchAvatarService")
        verifyCORSHeader(false)
      }
    }

    scenario("POST /") {
      Post() ~> routes ~> check {
        handled should be(false)
      }
    }

  }

}
