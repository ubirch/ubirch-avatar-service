package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.model.ContentTypes._
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
trait DeviceIdRoute extends MyJsonProtocol
  with CORSDirective {

  val route: Route = {

    respondWithCORS {
      path(device / Segment) { deviceId =>

        // TODO authentication for all three methods

        get {
          complete {
            DeviceManager.info(deviceId) match {

              case None =>
                val error = ErrorFactory.createString("QueryError", s"deviceId not found: deviceId=$deviceId")
                HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))

              case Some(deviceObject) => Some(deviceObject)

            }
          }
        } ~
          put {
            complete {

              DeviceManager.update(deviceId) match {

                case None =>
                  val error = ErrorFactory.createString("UpdateError", s"failed to update device: deviceId=$deviceId")
                  HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))

                case Some(deviceObject) => Some(deviceObject)
              }

            }
          } ~
          delete {
            complete {

              DeviceManager.delete(deviceId) match {

                case None =>
                  val error = ErrorFactory.createString("DeleteError", s"failed to delete device: deviceId=$deviceId")
                  HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))

                case Some(deviceObject) => Some(deviceObject)

              }

            }
          }

      }
    }

  }

}
