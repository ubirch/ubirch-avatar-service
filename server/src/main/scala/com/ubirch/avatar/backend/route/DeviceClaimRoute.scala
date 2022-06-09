package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{ Authorization, OAuth2BearerToken }
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.rest.device.{ DeviceClaim, DeviceUserClaim, DeviceUserClaimRequest }
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.JsonFormats
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.apache.http.HttpStatus
import org.json4s.Formats

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceClaimRoute(implicit system: ActorSystem) extends ResponseUtil with CORSDirective with StrictLogging {
  private val bearerToken = optionalHeaderValueByType(classOf[Authorization]).map(extractBearerToken)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit private val formatter: Formats = JsonFormats.default
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)
  private val deviceApiActor = system.actorSelection(ActorNames.DEVICE_API_PATH)

  val route: Route = {

    path(claim) {
      respondWithCORS {
        put {
          entity(as[DeviceClaim]) { deviceClaim =>
            {
              bearerToken { token =>
                claimDevice(token, deviceClaim)
              }
            }
          }
        }

      }
    }
  }

  private def claimDevice(token: Option[String], deviceClaim: DeviceClaim): Route = {
    token match {

      case None =>
        val errorRsp = JsonErrorResponse(errorType = "01", errorMessage = "Authorization header is missing.")
        complete(requestErrorResponse(errorRsp, StatusCodes.Unauthorized))

      case Some(value) if value != Config.trackleAuthToken =>
        val errorRsp = JsonErrorResponse(errorType = "01", errorMessage = "Authorization header value is wrong.")
        complete(requestErrorResponse(errorRsp, StatusCodes.Unauthorized))

      case Some(_) =>
        logger.info(s"PUT .../device/claim for $deviceClaim")
        onComplete(deviceApiActor ? DeviceUserClaimRequest(
          hwDeviceId = deviceClaim.hwDeviceId,
          userId = deviceClaim.userId)) {

          case Success(resp) =>
            resp match {
              case duc: DeviceUserClaim =>
                complete(HttpStatus.SC_OK -> duc)
              case jre: JsonErrorResponse =>
                complete(HttpStatus.SC_BAD_REQUEST -> jre)
              case _ =>
                complete(HttpStatus.SC_INTERNAL_SERVER_ERROR -> requestErrorResponse(
                  errorType = "DeviceClaimError",
                  errorMessage = s"could not claim device ${deviceClaim.hwDeviceId} for user ${deviceClaim.userId}"))
            }

          case Failure(t) =>
            logger.error("fetching device failed", t)
            complete(HttpStatus.SC_INTERNAL_SERVER_ERROR -> serverErrorResponse(
              errorType = "DeviceClaimError",
              errorMessage = t.getMessage))
        }
    }
  }

  private def extractBearerToken(authHeader: Option[Authorization]): Option[String] =
    authHeader.collect {
      case Authorization(OAuth2BearerToken(token)) => token
    }

}
