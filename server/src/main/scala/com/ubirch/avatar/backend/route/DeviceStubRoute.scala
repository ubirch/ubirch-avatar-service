package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceStubRoute(implicit httpClient: HttpExt, materializer: Materializer, system:ActorSystem)
  extends ResponseUtil
    with CORSDirective
    with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)


  private val oidcDirective = new OidcDirective()

  val route: Route = {

    path(stub) {
      respondWithCORS {
        oidcDirective.oidcToken2UserContext { userContext =>

          get {
            logger.error("Disabled Endpoint GET /stub by userContext was called, though it was only related to TrackleService's endpoint GET /sleepingPeriod/<id> ")
            complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))
            //            onComplete(deviceApiActor ? AllStubs(session = AvatarSession(userContext = userContext))) {
            //              case Success(resp) =>
            //                resp match {
            //                  case stubs: AllStubsResult => complete(stubs.stubs)
            //                  case _ => complete(requestErrorResponse(errorType = "DeviceStubError", errorMessage = "could not fetch device stubs"))
            //                }
            //
            //              case Failure(t) =>
            //                logger.error("fetching device failed", t)
            //                complete(serverErrorResponse(errorType = "DeviceStubError", errorMessage = t.getMessage))
            //            }
          }

        }
      }
    }

  }

}
