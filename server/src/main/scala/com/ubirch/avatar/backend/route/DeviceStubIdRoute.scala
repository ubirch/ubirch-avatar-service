package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.device.DeviceInfo
import com.ubirch.avatar.util.server.RouteConstants._
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
class DeviceStubIdRoute(implicit httpClient: HttpExt, materializer: Materializer, system:ActorSystem) extends ResponseUtil
    with CORSDirective
    with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

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
