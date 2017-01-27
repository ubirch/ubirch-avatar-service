package com.ubirch.avatar.backend.route

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes.Forbidden
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.backend.Actor.{CreateDevice, DeviceApiActor}
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.Device
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

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
