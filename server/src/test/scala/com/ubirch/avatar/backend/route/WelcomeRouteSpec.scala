package com.ubirch.avatar.backend.route

import com.ubirch.avatar.test.base.RouteSpec
import com.ubirch.util.model.JsonResponse

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.util.mongo.connection.MongoUtil

import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import play.api.libs.ws.WSClient
import play.api.libs.ws.ning.NingWSClient

/**
  * author: cvandrei
  * since: 2016-09-22
  */
class WelcomeRouteSpec extends RouteSpec {

  implicit val ws: WSClient = NingWSClient()
  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

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
