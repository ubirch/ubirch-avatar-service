package com.ubirch.avatar.backend.route

import com.ubirch.avatar.core.device.DeviceDataManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.{DeviceData, ErrorFactory}
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-30
  */
trait DeviceDataHistoryRoute extends MyJsonProtocol
  with CORSDirective {

  val route: Route = {

    // TODO authentication
    respondWithCORS {

      path(device / Segment / history) { deviceId =>
        get {
          onSuccess(queryHistory(deviceId)) {
            case None => complete(errorResponse(deviceId))
            case Some(deviceData) => complete(deviceData)
          }
        }

      } ~ pathPrefix(device / Segment / history) { deviceId =>

        path(IntNumber) { from =>
          get {
            onSuccess(queryHistory(deviceId, Some(from))) {
              case None => complete(errorResponse(deviceId, Some(from)))
              case Some(deviceData) => complete(deviceData)
            }
          }

        } ~ path(IntNumber / IntNumber) { (from, size) =>
          get {
            onSuccess(queryHistory(deviceId, Some(from), Some(size))) {
              case None => complete(errorResponse(deviceId, Some(from), Some(size)))
              case Some(deviceData) => complete(deviceData)
            }
          }

        }

      }

    }

  }

  private def queryHistory(deviceId: String,
                           fromOpt: Option[Int] = None,
                           sizeOpt: Option[Int] = None
                          ): Future[Option[Seq[DeviceData]]] = {

    val deviceData: Future[Seq[DeviceData]] = fromOpt match {

      case Some(from) =>
        sizeOpt match {
          case Some(size) => DeviceDataManager.history(deviceId, from, size)
          case None => DeviceDataManager.history(deviceId, from)
        }

      case None => DeviceDataManager.history(deviceId)

    }

    deviceData map {
      case seq if seq.isEmpty => None
      case seq => Some(seq)
    }

  }

  private def errorResponse(deviceId: String,
                            fromOpt: Option[Long] = None,
                            sizeOpt: Option[Long] = None
                           ): HttpResponse = {
    val error = ErrorFactory.createString("QueryError", s"deviceId not found: deviceId=$deviceId, from=$fromOpt, size=$sizeOpt")
    HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))
  }

}
