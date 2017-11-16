package com.ubirch.avatar.backend.route

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.ubirch.avatar.core.avatar.AvatarStateManagerREST
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.device.AvatarState
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * author: cvandrei
  * since: 2016-10-27
  */
class DeviceStateRoute(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer, system:ActorSystem)
  extends ResponseUtil
    with CORSDirective {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val oidcDirective = new OidcDirective()

  val route: Route = {

    path(JavaUUID / state) { deviceId =>
      respondWithCORS {
        oidcDirective.oidcToken2UserContext { userContext =>
          get {
            onSuccess(queryState(deviceId)) {
              case None =>
                complete(requestErrorResponse(errorType = "QueryError", errorMessage = s"deviceId not found: deviceId=$deviceId"))
              case Some(avatarState: AvatarState) =>
                complete(avatarState)
            }
          }
        }
      }
    }

  }

  private def queryState(deviceId: UUID)(implicit mongo: MongoUtil): Future[Option[AvatarState]] = {
    DeviceManager.info(deviceId).flatMap {
      case Some(dvc) =>
        AvatarStateManagerREST.byDeviceId(dvc.deviceId)
      case None =>
        Future(None)
    }
  }

  //  private def storeState(deviceId: UUID, state: DeviceState): Future[Option[DeviceState]] = Future(None) // TODO implementation

}