package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.device.DeviceInfo
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective

import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
trait DeviceStubRoute
  extends MyJsonProtocol
    with ResponseUtil
    with CORSDirective
    with StrictLogging {

  implicit val system = ActorSystem()

  private val oidcDirective = new OidcDirective()

  val route: Route = {

    path(stub) {
      respondWithCORS {
        oidcDirective.oidcToken2UserContext { userContext =>
          get {
            onComplete(DeviceManager.allStubs()) {
              case Success(resp) =>
                resp match {
                  case stubs: Seq[DeviceInfo] =>
                    complete(stubs)
                  case _ =>
                    complete(requestErrorResponse(errorType = "DeviceStubError", errorMessage = "could not fetch device stubs"))
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
}
