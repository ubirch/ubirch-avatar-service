package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.test.base.RouteSpec
import com.ubirch.util.model.JsonResponse
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-22
  */
class WelcomeRouteSpec extends RouteSpec {

  private val mainRoute = new MainRoute
  private val routes = mainRoute.myRoute

  feature(s"call health/welcome page") {

    scenario("GET /") {
      Get() ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        val goInfo = s"${Config.goPipelineName} / ${Config.goPipelineLabel} / ${Config.goPipelineRevision}"
        responseAs[JsonResponse] shouldEqual JsonResponse(message = s"Welcome to the ubirchAvatarService ( $goInfo )")
        verifyCORSHeader(exist = false)
      }
    }

    scenario("POST /") {
      Post() ~> routes ~> check {
        handled should be(false)
      }
    }

  }


}
