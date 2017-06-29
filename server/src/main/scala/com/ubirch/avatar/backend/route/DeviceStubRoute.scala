package com.ubirch.avatar.backend.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.backend.actor.{AllStubs, AllStubsResult, DeviceApiActor}
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.AvatarSession
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceStubRoute(implicit ws: WSClient)
  extends ResponseUtil
    with CORSDirective
    with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val deviceApiActor = system.actorOf(Props(new DeviceApiActor), ActorNames.DEVICE_API)

  private val oidcDirective = new OidcDirective()

  val route: Route = {

    path(stub) {
      respondWithCORS {
        oidcDirective.oidcToken2UserContext { userContext =>

          get {
            onComplete(deviceApiActor ? AllStubs(session = AvatarSession(userContext = userContext))) {

              case Success(resp) =>
                resp match {
                  case stubs: AllStubsResult => complete(stubs.stubs)
                  case _ => complete(requestErrorResponse(errorType = "DeviceStubError", errorMessage = "could not fetch device stubs"))
                }

              case Failure(t) =>
                logger.error("fetching device failed", t)
                complete(serverErrorResponse(errorType = "DeviceStubError", errorMessage = t.getMessage))

            }
          }

        }
      }
    }

  }

}
