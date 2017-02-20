package com.ubirch.avatar.backend.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.http.scaladsl.model.StatusCodes.Forbidden
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route

import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait ForbiddenRoute extends MyJsonProtocol
  with CORSDirective
  with ResponseUtil
  with StrictLogging {

  val route: Route = respondWithCORS {
    get {
      complete(HttpResponse(status = Forbidden, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Tür ist zu!")))
    }
  }
}
