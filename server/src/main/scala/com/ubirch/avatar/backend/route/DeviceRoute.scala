package com.ubirch.avatar.backend.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.backend.actor.{AllDevices, CreateDevice, CreateResult, DeviceApiActor}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.rest.device.Device
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.AvatarSession
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceRoute(implicit ws: StandaloneWSClient) extends MyJsonProtocol
  with CORSDirective
  with ResponseUtil
  with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val deviceApiActor = system.actorOf(Props(new DeviceApiActor), ActorNames.DEVICE_API)

  private val oidcDirective = new OidcDirective()

  val route: Route =

    respondWithCORS {
      oidcDirective.oidcToken2UserContext { userContext =>

        get {

          onComplete(deviceApiActor ? AllDevices(session = AvatarSession(userContext = userContext))) {

            case Success(resp) =>
              resp match {
                case devices: Seq[Device] => complete(devices)
                case _ => complete(serverErrorResponse(errorType = "QueryError", errorMessage = "DeviceRoute.post failed with unhandled message"))
              }

            case Failure(t) =>
              logger.error("querying all devices failed", t)
              complete(serverErrorResponse(errorType = "QueryError", errorMessage = t.getMessage))

          }

        } ~ post {

          entity(as[Device]) { device =>

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
