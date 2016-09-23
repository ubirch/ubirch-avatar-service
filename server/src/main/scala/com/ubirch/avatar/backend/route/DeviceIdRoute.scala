package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.core.server.util.RouteConstants._
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
              case None => BadRequest // TODO add error json to response
              case Some(deviceObject) => Some(deviceObject)
            }
          }
        } ~
          put {
            complete {
              DeviceManager.update(deviceId) match {
                case None => BadRequest // TODO add error json to response
                case Some(deviceObject) => Some(deviceObject)
              }
            }
          } ~
          delete {
            complete {
              DeviceManager.delete(deviceId) match {
                case None => BadRequest // TODO add error json to response
                case Some(deviceObject) => Some(deviceObject)
              }
            }
          }

      }
    }

  }

}
