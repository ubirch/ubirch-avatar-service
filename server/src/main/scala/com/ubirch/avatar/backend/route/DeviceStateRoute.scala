package com.ubirch.avatar.backend.route

import java.util.UUID

import com.ubirch.avatar.awsiot.services.AwsShadowService
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.aws.ThingShadowState
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import com.ubirch.util.http.response.ResponseUtil
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-10-27
  */
trait DeviceStateRoute extends MyJsonProtocol
  with ResponseUtil
  with CORSDirective {

  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val route: Route = {

    path(JavaUUID / state) { deviceId =>
      respondWithCORS {
        get {
          onSuccess(queryState(deviceId)) {
            case None =>
              complete(requestErrorResponse(errorType = "QueryError", errorMessage = s"deviceId not found: deviceId=$deviceId"))
            case Some(deviceState: ThingShadowState) =>
              complete(deviceState)
          }
        }
        //        ~ post {
        //          entity(as[ThingShadowState]) { state =>
        //            onSuccess(storeState(deviceId, state)) {
        //              case None => complete(errorResponse(deviceId))
        //              case Some(storedData) => complete(storedData)
        //            }
      }
    }

  }

  private def queryState(deviceId: UUID): Future[Option[ThingShadowState]] = {
    DeviceManager.info(deviceId).map {
      case Some(dvc) =>
        Some(AwsShadowService.getCurrentDeviceState(dvc.awsDeviceThingId))
      case None =>
        None
    }
  }

  //  private def storeState(deviceId: UUID, state: DeviceState): Future[Option[DeviceState]] = Future(None) // TODO implementation
  //
  //  private def errorResponse(deviceId: UUID,
  //                            fromOpt: Option[Long] = None,
  //                            sizeOpt: Option[Long] = None
  //                           ): HttpResponse = {
  //    val error = ErrorFactory.createString("QueryError", s"deviceId not found: deviceId=$deviceId, from=$fromOpt, size=$sizeOpt")
  //    HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))
  //  }
  //  //  private def storeState(deviceId: String, state: DeviceState): Future[Option[DeviceState]] = Future(None) // TODO implementation
}