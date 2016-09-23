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
trait DeviceStubIdRoute extends MyJsonProtocol
  with CORSDirective {

  val route: Route = {

    // TODO authentication

    respondWithCORS {
      path(device / stub / Segment) { deviceId =>
        get {
          complete {
            DeviceManager.shortInfo(deviceId) match {
              case None => BadRequest // TODO add error json to response
              case Some(deviceObject) => Some(deviceObject)
            }
          }
        }
      }
    }

  }

}
