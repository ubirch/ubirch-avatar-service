package com.ubirch.avatar.backend.route

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.backend.actor.{CreateDevice, DeviceApiActor}
import com.ubirch.avatar.config.{Config, ConfigKeys}
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.device.Device
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.redis.RedisClientUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import redis.RedisClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

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
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val deviceApiActor = system.actorOf(Props[DeviceApiActor], ActorNames.DEVICE_API)

  private val oidcDirective = new OidcDirective()

  val route: Route = {
    path(JavaUUID) { deviceId =>

      // TODO authentication for all three methods

      respondWithCORS {
        oidcDirective.oidcToken2UserContext { userContext =>
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
          } ~ post {
            entity(as[Device]) { device =>
              onComplete(deviceApiActor ? CreateDevice(device = device)) {
                case Success(resp) =>
                  resp match {
                    case dev: Device =>
                      complete(dev)
                    case jer: JsonErrorResponse =>
                      complete(requestErrorResponse(jer))
                    case _ =>
                      complete("doof")
                  }
                case Failure(t) =>
                  logger.error("device creation failed", t)
                  complete(serverErrorResponse(errorType = "CreationError", errorMessage = t.getMessage))
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
}