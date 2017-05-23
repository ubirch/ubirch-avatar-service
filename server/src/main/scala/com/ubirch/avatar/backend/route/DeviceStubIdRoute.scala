package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.device.DeviceInfo
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective

import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceStubIdRoute
  extends MyJsonProtocol
    with ResponseUtil
    with CORSDirective
    with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val oidcDirective = new OidcDirective()

  val route: Route =

    path(stub / JavaUUID) { deviceId =>
      respondWithCORS {
        oidcDirective.oidcToken2UserContext { userContext =>
          get {
            onComplete(DeviceManager.stub(deviceId)) {
              case Success(resp) =>
                resp match {
                  case Some(stub: DeviceInfo) =>
                    complete(stub)
                  case _ =>
                    complete(requestErrorResponse(errorType = "QueryError", errorMessage = s"deviceId not found: deviceId=$deviceId"))
                }
              case Failure(t) =>
                logger.error("device creation failed", t)
                complete(serverErrorResponse(errorType = "DeviceStubError", errorMessage = t.getMessage))
            }

          }
        }
      }
    }
}
