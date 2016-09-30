package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.core.device.DeviceDataManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.{DeviceData, ErrorFactory}
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

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
          complete {
            val deviceDataOpt = queryHistory(deviceId)
            deviceDataOpt match {
              case None => errorResponse(deviceId)
              case Some(deviceData) => deviceData
            }
          }
        }

      } ~ pathPrefix(device / Segment / history) { deviceId =>

        path(LongNumber) { from =>
          get {
            complete {
              val deviceDataOpt = queryHistory(deviceId, Some(from))
              deviceDataOpt match {
                case None => errorResponse(deviceId, Some(from))
                case Some(deviceData) => deviceData
              }
            }
          }

        } ~ path(LongNumber / LongNumber) { (from, size) =>
          get {
            complete {
              val deviceDataOpt = queryHistory(deviceId, Some(from), Some(size))
              deviceDataOpt match {
                case None => errorResponse(deviceId, Some(from), Some(size))
                case Some(deviceData) => deviceData
              }
            }
          }

        }

      }

    }

  }

  private def queryHistory(deviceId: String,
                           fromOpt: Option[Long] = None,
                           sizeOpt: Option[Long] = None
                          ): Option[Seq[DeviceData]] = {

    val deviceData = fromOpt match {

      case Some(from) =>
        sizeOpt match {
          case Some(size) => DeviceDataManager.history(deviceId, from, size)
          case None => DeviceDataManager.history(deviceId, from)
        }

      case None => DeviceDataManager.history(deviceId)

    }

    deviceData.isEmpty match {
      case true => None
      case false => Some(deviceData)
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
