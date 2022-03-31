package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.core.avatar.AvatarStateManagerREST
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.device.AvatarState
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective

import java.util.UUID
import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * author: cvandrei
  * since: 2016-10-27
  */
class DeviceStateRoute(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer, system:ActorSystem)
  extends ResponseUtil
    with CORSDirective with StrictLogging with RouteAnalyzingByLogsSupport {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val route: Route = {

    path(JavaUUID / state) { deviceId =>
      respondWithCORS {

        get {
          logger.error("Disabled Endpoint GET /<deviceId>/state was called, though it shouldn't be used anymore")
          complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))
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


}