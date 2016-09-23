package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.ErrorFactory
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceStubIdRoute extends MyJsonProtocol
  with CORSDirective {

  val route: Route = {

    // TODO authentication

    respondWithCORS {
      path(device / stub / Segment) { deviceId =>
        get {
          complete {

            DeviceManager.shortInfo(deviceId) match {

              case None =>
                val error = ErrorFactory.createString("QueryError", s"deviceId not found: deviceId=$deviceId")
                HttpResponse(status = BadRequest, entity = HttpEntity(ContentTypes.`application/json`, error))

              case Some(deviceObject) => Some(deviceObject)

            }

          }
        }
      }
    }

  }

}
