package com.ubirch.avatar.backend.route

import com.typesafe.scalalogging.LazyLogging

import com.ubirch.avatar.backend.ResponseUtil
import com.ubirch.avatar.core.device.DeviceTypeManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.device.DeviceType
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-11-09
  */
trait DeviceTypeRoute extends CORSDirective
  with MyJsonProtocol
  with ResponseUtil
  with LazyLogging {

  implicit val system = ActorSystem()

  val route: Route = {

    path(deviceType) {

      respondWithCORS {

        get {
          complete {
            onComplete(DeviceTypeManager.all()) {

              case Success(res) => complete(res)

              case Failure(t) =>
                logger.error(s"failed to query all device types", t)
                complete(serverErrorResponse("QueryError", errorMessage = t.getMessage))

            }
          }

        } ~ post {
          entity(as[DeviceType]) { postDeviceType =>
            onComplete(DeviceTypeManager.create(postDeviceType)) {

              case Success(resp) => complete(resp)

              case Failure(t) =>
                logger.error(s"deviceType creation failed: deviceType=$postDeviceType", t)
                complete(serverErrorResponse(errorType = "CreationError", errorMessage = t.getMessage))

            }
          }

        } ~ put {
          entity(as[DeviceType]) { postDeviceType =>
            onComplete(DeviceTypeManager.update(postDeviceType)) {

              case Success(resp) => complete(resp)

              case Failure(t) =>
                logger.error(s"deviceType update failed: deviceType=$postDeviceType", t)
                complete(serverErrorResponse(errorType = "UpdateError", errorMessage = t.getMessage))

            }
          }

        }

      }
    } ~ path(deviceType / init) {

      get {
        complete {
          onComplete(DeviceTypeManager.init()) {

            case Success(resp) => complete(resp)

            case Failure(t) =>
              logger.error("failed to create default deviceTypes", t)
              complete(serverErrorResponse(errorType = "CreationError", errorMessage = t.getMessage))

          }
        }
      }

    }
  }

}
