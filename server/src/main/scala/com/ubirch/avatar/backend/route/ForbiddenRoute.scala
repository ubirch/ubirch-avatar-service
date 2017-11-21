package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.StatusCodes.Forbidden
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.rest.akka.directives.CORSDirective

import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait ForbiddenRoute extends ResponseUtil
  with CORSDirective
  with StrictLogging {

  val route: Route = respondWithCORS {
    get {
      complete(HttpResponse(status = Forbidden, entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "Tür ist zu!")))
    }
  }
}
