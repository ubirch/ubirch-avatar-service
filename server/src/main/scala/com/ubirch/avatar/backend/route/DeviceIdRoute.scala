package com.ubirch.avatar.backend.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.Device
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceIdRoute extends CORSDirective
  with MyJsonProtocol
  with ResponseUtil
  with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(15 seconds)

  val route: Route = {
    path(JavaUUID) { deviceId =>

      // TODO authentication for all three methods

      respondWithCORS {
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
        } ~ put {
          entity(as[Device]) { device =>
            complete {
              DeviceManager.info(deviceId).map {
                case None =>
                  requestErrorResponse(
                    errorType = "UpdateError",
                    errorMessage = s"update non existing device: deviceId=$deviceId"
                  )
                case Some(dev) if deviceId.toString != device.deviceId =>
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
                    case Some(deviceObject) => deviceObject
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
}