package com.ubirch.avatar.backend.route

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.backend.actor.{CreateDevice, CreateResult, DeviceApiActor}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.AvatarSession
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceIdRoute(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer)
  extends ResponseUtil
    with CORSDirective
    with StrictLogging {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val deviceApiActor = system.actorOf(Props(new DeviceApiActor), ActorNames.DEVICE_API)

  private val oidcDirective = new OidcDirective()

  val route: Route = {
    path(JavaUUID) { deviceId =>

      respondWithCORS {
        oidcDirective.oidcToken2UserContext { userContext =>

          get {

            getDeviceInfo(deviceId)

          } ~ post {

            entity(as[Device]) { device =>
              postDevice(device, AvatarSession(userContext))
            }

          } ~ put {

            entity(as[Device]) { device =>
              updateDevice(deviceId, device)
            }

          } ~ delete {

            deleteDevice(deviceId)

          }

        }
      }

    }
  }

  private def getDeviceInfo(deviceId: UUID) = {

    onComplete(DeviceManager.info(deviceId)) {

      case Failure(t) =>
        logger.error("get device failed", t)
        complete(serverErrorResponse(errorType = "QueryError", errorMessage = t.getMessage))

      case Success(resp) =>

        resp match {

          case None =>
            complete(
              requestErrorResponse(
                errorType = "QueryError",
                errorMessage = s"deviceId not found: deviceId=$deviceId"
              )
            )

          case Some(device) => complete(device)

        }

    }

  }

  private def postDevice(device: Device, avatarSession: AvatarSession): Route = {

    onComplete(deviceApiActor ? CreateDevice(session = avatarSession, device = device)) {

      case Success(resp) =>
        resp match {

          case result: CreateResult if result.device.isDefined =>
            complete(result.device.get)

          case result: CreateResult if result.error.isDefined =>
            complete(requestErrorResponse(result.error.get))

          case result: CreateResult =>
            logger.error(s"unhandled CreateResult: createResult=$result")
            complete(serverErrorResponse(errorType = "CreationError", errorMessage = "DeviceIdRoute.post failed with unhandled case"))

          case _ =>
            complete(serverErrorResponse(errorType = "CreationError", errorMessage = "DeviceIDRoute.post failed with unhandled message"))

        }

      case Failure(t) =>
        logger.error("device creation failed", t)
        complete(serverErrorResponse(errorType = "CreationError", errorMessage = t.getMessage))

    }

  }

  private def updateDevice(deviceId: UUID, device: Device): Route = {

    val foo: Future[DeviceIdResult] = DeviceManager.info(deviceId).flatMap {

      case None =>

        Future(
          DeviceIdResult(
            error = Some(
              requestErrorResponse(
                errorType = "UpdateError",
                errorMessage = s"update non existing device: deviceId=$deviceId"
              )
            )
          )
        )

      case Some(dev) if dev.deviceId != device.deviceId =>

        Future(
          DeviceIdResult(
            error = Some(
              requestErrorResponse(
                errorType = "UpdateError",
                errorMessage = s"deviceId mismatch $deviceId <-> device: deviceId=$deviceId"
              )
            )
          )
        )

      case Some(dev) =>

        DeviceManager.update(device = device).map {

          case None =>
            logger.debug(s"PUT /device/:id -- None (device=$device)")
            DeviceIdResult(
              error = Some(
                requestErrorResponse(
                  errorType = "UpdateError",
                  errorMessage = s"failed to update device: deviceId=$deviceId"
                )
              )
            )

          case Some(deviceObject) =>
            logger.debug(s"PUT /device/:id -- updateDevice=$deviceObject")
            DeviceIdResult(device = Some(deviceObject))

        }

    }

    onComplete(foo) {

      case Failure(t) =>
        logger.error("get device failed", t)
        complete(serverErrorResponse(errorType = "QueryError", errorMessage = t.getMessage))

      case Success(resp) =>

        if (resp.device.isDefined) {
          complete(resp.device.get)
        } else if (resp.error.isDefined) {
          complete(resp.error.get)
        } else {
          complete(serverErrorResponse(errorType = "ServerError", errorMessage = "this should never have happened: method=DeviceIdRoute.updateDevice()"))
        }

    }

  }

  private def deleteDevice(deviceId: UUID): Route = {

    val result = DeviceManager.info(deviceId) flatMap {

      case None =>

        Future(
          DeviceIdResult(
            error = Some(
              requestErrorResponse(
                errorType = "DeleteError",
                errorMessage = s"delete a non existing device: deviceId=$deviceId"
              )
            )
          )
        )

      case Some(existingDevice) =>

        DeviceManager.delete(existingDevice) map {

          case None =>

            DeviceIdResult(
              error = Some(
                requestErrorResponse(
                  errorType = "DeleteError",
                  errorMessage = s"failed to delete device: deviceId=$deviceId"
                )
              )
            )

          case Some(deleted) => DeviceIdResult(device = Some(deleted))

        }

    }

    onComplete(result) {

      case Failure(t) =>
        logger.error("delete device failed", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = t.getMessage))

      case Success(resp) =>

        if (resp.device.isDefined) {
          complete(resp.device.get)
        } else if (resp.error.isDefined) {
          complete(resp.error.get)
        } else {
          complete(serverErrorResponse(errorType = "ServerError", errorMessage = "this should never have happened: method=DeviceIdRoute.deleteDevice()"))
        }

    }

  }

}

case class DeviceIdResult(device: Option[Device] = None,
                          error: Option[HttpResponse] = None
                         )