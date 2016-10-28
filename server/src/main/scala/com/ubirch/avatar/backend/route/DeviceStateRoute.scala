package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.backend.aws.services.ShadowService
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.aws.ThingShadowState
import com.ubirch.avatar.model.{DeviceState, ErrorFactory}
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-10-27
  */
trait DeviceStateRoute extends MyJsonProtocol
  with CORSDirective {

  implicit val system = ActorSystem()

  import system.dispatcher

  val route: Route = {

    path(device / Segment / state) { deviceId =>

      get {
        onSuccess(queryState(deviceId)) {
          case None => complete(errorResponse(deviceId))
          case Some(deviceState) => complete(deviceState)
        }
      } ~ post {
        entity(as[DeviceState]) { state =>
          onSuccess(storeState(deviceId, state)) {
            case None => complete(errorResponse(deviceId))
            case Some(storedData) => complete(storedData)
          }
        }
      }

    }

  }

  private def queryState(deviceId: String): Future[Option[ThingShadowState]] = {
    DeviceManager.shortInfo(deviceId).map {
      case Some(device) =>
        Some(ShadowService.getCurrentDeviceState(device.awsDeviceThingId))
      case None =>
        None
    }
  }

  private def storeState(deviceId: String, state: DeviceState): Future[Option[DeviceState]] = Future(None) // TODO implementation

  private def errorResponse(deviceId: String,
                            fromOpt: Option[Long] = None,
                            sizeOpt: Option[Long] = None
                           ): HttpResponse = {
    val error = ErrorFactory.createString("QueryError", s"deviceId not found: deviceId=$deviceId, from=$fromOpt, size=$sizeOpt")
    HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))
  }

}