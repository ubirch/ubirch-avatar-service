package com.ubirch.avatar.backend.route

import akka.http.scaladsl.server.Route
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.Device
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import akka.http.scaladsl.model.StatusCodes._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceRoute extends MyJsonProtocol
  with CORSDirective {

  val route: Route = {

    respondWithCORS {
      path(device) {

        // TODO authentication for all methods...or just for post?
        get {
          complete(DeviceManager.all())
        } ~
          post {
            entity(as[Device]) { device =>
              complete {
                DeviceManager.create(device) match {
                  case None => BadRequest // TODO add error json to response
                  case Some(deviceObject) => Some(deviceObject)
                }
              }
            }
          }

      }
    }

  }

}
