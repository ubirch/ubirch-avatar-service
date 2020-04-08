package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.backend.actor._
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.AvatarSession
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceRoute(implicit httpClient: HttpExt, materializer: Materializer, system: ActorSystem)
  extends ResponseUtil
    with CORSDirective
    with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val deviceApiActor = system.actorSelection(ActorNames.DEVICE_API_PATH)

  private val oidcDirective = new OidcDirective()

  val route: Route =
    respondWithCORS {
      oidcDirective.oidcToken2UserContext { userContext =>

        get {
          logger.info(s"GET /device by userContext: $userContext")
          onComplete(deviceApiActor ? AllDevices(session = AvatarSession(userContext = userContext))) {

            case Success(resp) =>
              resp match {
                case devices: AllDevicesResult =>
                  logger.debug(s"returning ${devices.devices}")
                  complete(StatusCodes.OK -> devices.devices)
                case _ => complete(serverErrorResponse(errorType = "QueryError", errorMessage = "DeviceRoute.post failed with unhandled message"))
              }

            case Failure(t) =>
              logger.error("querying all devices failed", t)
              complete(serverErrorResponse(errorType = "QueryError", errorMessage = t.getMessage))

          }

        } ~ post {

          entity(as[Device]) { device =>
            logger.info(s"POST /device with device: $device")

            val avatarSession = AvatarSession(userContext)
            onComplete(deviceApiActor ? CreateDevice(session = avatarSession, device = device)) {

              case Success(resp) =>
                resp match {

                  case result: CreateResult if result.device.isDefined =>
                    complete(result.device.get)

                  case result: CreateResult if result.error.isDefined =>
                    complete(requestErrorResponse(result.error.get))

                  case result: CreateResult =>
                    logger.error(s"unhandled CreateResult: createResult=$result")
                    complete(serverErrorResponse(errorType = "CreationError", errorMessage = "DeviceRoute.post failed with unhandled case"))

                  case _ =>
                    complete(serverErrorResponse(errorType = "CreationError", errorMessage = "DeviceRoute.post failed with unhandled message"))

                }

              case Failure(t) =>
                logger.error("device creation failed", t)
                complete(serverErrorResponse(errorType = "CreationError", errorMessage = t.getMessage))

            }

          }

        }

      }
    }

}
