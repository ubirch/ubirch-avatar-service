package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.backend.actor.{CreateDevice, CreateResult}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.AvatarSession
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceIdRoute(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer, system: ActorSystem)
  extends ResponseUtil
    with CORSDirective
    with StrictLogging with RouteAnalyzingByLogsSupport {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val deviceApiActor = system.actorSelection(ActorNames.DEVICE_API_PATH)

  private val oidcDirective = new OidcDirective()

  val route: Route = {
    path(JavaUUID) { deviceId =>

      respondWithCORS {
        oidcDirective.oidcToken2UserContext { userContext =>

          get {
            //Still being used 24.2.2022
            logger.info(s"GET .../device/$deviceId")
            getDeviceInfo(deviceId)

          } ~ post {
            //Not being used 24.2.2022
            // TODO the given deviceId from the url path is being ignored and this duplicated `POST device` --> can we delete this code?
            entity(as[Device]) { device =>
              logger.info(s"Endpoint POST .../device/id for device: $device $NOT_EXPECTED_TO_BE_USED_ANYMORE")
              postDevice(device, AvatarSession(userContext))
            }
          } ~ put {
            //Is being used 24.02.2022, probably to update EOL flag.
            // TODO suggestion: move this to `PUT /device` since otherwise we need additional logic to check that the given deviceId matches the one provided in the json
            entity(as[Device]) { device =>
              logger.info(s"PUT .../device/id for device: $device")
              updateDevice(deviceId, device)
            }
          } ~ delete {
            logger.error(s"Disabled endpoint GET /device/id with id $deviceId was called by ${userContext.userId}")
            complete(requestErrorResponse(errorType = "Disabled endpoint error", errorMessage =
              "this endpoint has been disabled together with TrackleService's endpoint /device/statistics/create"))
            //Disabled as the related TrackleService's Delete User endpoint is not being used at the moment.
            // deleteDevice(deviceId)
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
            logger.debug(s"no device will be returned for deviceId: $deviceId")
            complete(requestErrorResponse(errorType = "QueryError", errorMessage = s"deviceId not found: deviceId=$deviceId"))

          case Some(device: Device) =>
            logger.debug(s"returning device: $device")
            complete(StatusCodes.OK -> device)

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

      case Some(_) =>

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

      case None => Future(DeviceIdResult(deviceDeleted = true))

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

          case Some(_) => DeviceIdResult(deviceDeleted = true)
        }
    }

    onComplete(result) {

      case Failure(t) =>
        logger.error("device deletion failed", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = t.getMessage))

      case Success(resp) =>

        if (resp.deviceDeleted) {
          complete(StatusCodes.OK)
        } else if (resp.error.isDefined) {
          complete(resp.error.get)
        } else {
          complete(serverErrorResponse(errorType = "ServerError", errorMessage = "this should never have happened: method=DeviceIdRoute.deleteDevice()"))
        }

    }

  }

}

case class DeviceIdResult(device: Option[Device] = None,
                          deviceDeleted: Boolean = false,
                          error: Option[HttpResponse] = None
                         )