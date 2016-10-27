package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.ErrorFactory.createString
import com.ubirch.avatar.model.{Device, JsonMessageResponse}
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceIdRoute extends CORSDirective
  with MyJsonProtocol {

  implicit val ec = scala.concurrent.ExecutionContext.global

  val route: Route = respondWithCORS {
    path(device / Segment) { deviceId =>

      // TODO authentication for all three methods

      get {
        onSuccess(DeviceManager.info(deviceId)) {
          case None =>
            val error = createString("QueryError", s"deviceId not found: deviceId=$deviceId")
            complete(HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error)))

          case Some(deviceObject) => complete(Some(deviceObject))

        }
      } ~
        put {
          entity(as[Device]) { device =>
            complete {
              DeviceManager.info(deviceId).map {
                case None =>
                  val error = createString("UpdateError", s"update non existing device: deviceId=$deviceId")
                  complete(HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error)))
                case Some(dev) if deviceId != device.deviceId =>
                  val error: String = createString("UpdateError", s"deviceId mismatch $deviceId <-> device: deviceId=$deviceId")
                  complete(HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error)))
                case Some(dev) =>
                  DeviceManager.update(device = device).map {
                    case None =>
                      val error = createString("UpdateError", s"failed to update device: deviceId=$deviceId")
                      HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))
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
                val error = createString("UpdateError", s"update non existing device: deviceId=$deviceId")
                complete(HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error)))
              case Some(existingDevice) =>
                DeviceManager.delete(existingDevice).map {
                  case None =>
                    val error = createString("DeleteError", s"failed to delete device: deviceId=$deviceId")
                    HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))
                  case Some(deletedDevice) =>
                    JsonMessageResponse(message = s"deleted device: $existingDevice")
                }
            }
          }
        }
    }
  }
}
