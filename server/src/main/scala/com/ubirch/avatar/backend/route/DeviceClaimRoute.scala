package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.rest.device.{DeviceClaim, DeviceUserClaim, DeviceUserClaimRequest}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.apache.http.HttpStatus

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceClaimRoute(implicit mongo: MongoUtil,
                       httpClient: HttpExt,
                       materializer: Materializer,
                       system: ActorSystem)
  extends ResponseUtil
    with CORSDirective
    with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val deviceApiActor = system.actorSelection(ActorNames.DEVICE_API_PATH)

  private val oidcDirective = new OidcDirective()

  val route: Route = {

    path(claim) {
      respondWithCORS {
        oidcDirective.oidcToken2UserContext { userContext =>

          put {

            entity(as[DeviceClaim]) { deviceClaim =>

              onComplete(deviceApiActor ? DeviceUserClaimRequest(hwDeviceId = deviceClaim.hwDeviceId, externalId = userContext.externalUserId, userId = userContext.userId)) {


                case Success(resp) =>
                  resp match {
                    case duc: DeviceUserClaim =>
                      complete(HttpStatus.SC_ACCEPTED -> duc)
                    case jre: JsonErrorResponse =>
                      complete(HttpStatus.SC_BAD_REQUEST -> jre)
                    case _ =>
                      complete(HttpStatus.SC_INTERNAL_SERVER_ERROR -> requestErrorResponse(errorType = "DeviceClaimError", errorMessage = s"could not claim device ${deviceClaim.hwDeviceId} for user ${userContext.externalUserId}"))
                  }

                case Failure(t) =>
                  logger.error("fetching device failed", t)
                  complete(HttpStatus.SC_INTERNAL_SERVER_ERROR -> serverErrorResponse(errorType = "DeviceClaimError", errorMessage = t.getMessage))

              }
            }
          }

        }
      }
    }

  }

}
