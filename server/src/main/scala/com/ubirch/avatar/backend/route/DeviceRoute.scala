package com.ubirch.avatar.backend.route


import com.ubirch.avatar.model.device.Device

import akka.http.scaladsl.server.Route
import com.ubirch.avatar.backend.ResponseUtil
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceRoute extends MyJsonProtocol
  with CORSDirective
  with ResponseUtil {

  implicit val ec = scala.concurrent.ExecutionContext.global

  val route: Route = respondWithCORS {
    path(device) {

      // TODO authentication for all methods...or just for post?
      get {

        complete(DeviceManager.all())

      } ~
        post {

          entity(as[Device]) { device =>
            complete {
              DeviceManager.createWithShadow(device).map {
                case None =>
                  requestErrorResponse(
                    errorType = "CreationError",
                    errorMessage = s"failed to create device: ${entity(as[String])}"
                  )
                case Some(deviceObject) => Some(deviceObject)
              }
            }
          }
        }
    }
  }
}
