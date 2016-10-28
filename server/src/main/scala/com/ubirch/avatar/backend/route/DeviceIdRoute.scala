package com.ubirch.avatar.backend.route

import akka.http.scaladsl.server.Route
import com.ubirch.avatar.backend.ResponseUtil
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.Device
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceIdRoute extends CORSDirective
  with MyJsonProtocol
  with ResponseUtil {

  implicit val ec = scala.concurrent.ExecutionContext.global

  val route: Route = respondWithCORS {
    path(device / Segment) { deviceId =>

      // TODO authentication for all three methods
      get {
        complete {
          DeviceManager.info(deviceId).map {
            case None =>
              requestErrorResponse(
                errorType = "QueryError",
                errorMessage = s"deviceId not found: deviceId=$deviceId"
              )
            case Some(device) =>
              Some(device)
          }
        }
      } ~
        put {
          entity(as[Device]) { device =>
            complete {
              DeviceManager.info(deviceId).map {
                case None =>
                  requestErrorResponse(
                    errorType = "UpdateError",
                    errorMessage = s"update non existing device: deviceId=$deviceId"
                  )
                case Some(dev) if deviceId != device.deviceId =>
                  requestErrorResponse(
                    errorType = "UpdateError",
                    errorMessage = s"deviceId mismatch $deviceId <-> device: deviceId=$deviceId"
                  )
                case Some(dev) =>
                  DeviceManager.update(device = device).map {
                    case None =>
                      requestErrorResponse(
                        errorType = "UpdateError",
                        errorMessage = s"failed to update device: deviceId=$deviceId"
                      )
                    case Some(deviceObject) => Some(deviceObject)
                  }
              }
            }
          }
        } ~
        delete {
          complete {
            DeviceManager.info(deviceId).map {
              case None =>
                requestErrorResponse(
                  errorType = "DeleteError",
                  errorMessage = s"delete a non existing device: deviceId=$deviceId"
                )
              case Some(existingDevice) =>
                DeviceManager.delete(existingDevice).map {
                  case None =>
                    requestErrorResponse(
                      errorType = "DeleteError",
                      errorMessage = s"failed to delete device: deviceId=$deviceId"
                    )
                  case Some(deletedDevice) =>
                    response(message = s"deleted device: $existingDevice")
                }
            }
          }
        }
    }
  }
}