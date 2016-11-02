package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.avatar.model.util.ErrorFactory
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-30
  */
trait DeviceDataHistoryRoute extends MyJsonProtocol
  with CORSDirective {

  implicit val system = ActorSystem()

  import system.dispatcher

  val route: Route = {

    // TODO authentication
    respondWithCORS {

      pathPrefix(device / Segment / data) { deviceId =>

        path(history) {
          get {
            onSuccess(queryHistory(deviceId)) {
              case None => complete(errorResponseHistory(deviceId))
              case Some(deviceData) => complete(deviceData)
            }
          }

        } ~ pathPrefix(history) {

          path(IntNumber) { from =>
            get {
              onSuccess(queryHistory(deviceId, Some(from))) {
                case None => complete(errorResponseHistory(deviceId, Some(from)))
                case Some(deviceData) => complete(deviceData)
              }
            }

          } ~ path(IntNumber / IntNumber) { (from, size) =>
            get {
              onSuccess(queryHistory(deviceId, Some(from), Some(size))) {
                case None => complete(errorResponseHistory(deviceId, Some(from), Some(size)))
                case Some(deviceData) => complete(deviceData)
              }
            }

          }

        }

      }

    }

  }

  private def queryHistory(deviceId: String,
                           fromOpt: Option[Int] = None,
                           sizeOpt: Option[Int] = None
                          ): Future[Option[Seq[DeviceDataRaw]]] = {

    val deviceData: Future[Seq[DeviceDataRaw]] = fromOpt match {

      case Some(from) =>
        sizeOpt match {
          case Some(size) => DeviceDataRawManager.history(deviceId, from, size)
          case None => DeviceDataRawManager.history(deviceId, from)
        }

      case None => DeviceDataRawManager.history(deviceId)

    }

    deviceData map {
      case seq if seq.isEmpty => None
      case seq => Some(seq)
    }

  }

  private def errorResponseHistory(deviceId: String,
                                   fromOpt: Option[Long] = None,
                                   sizeOpt: Option[Long] = None
                                  ): HttpResponse = {
    val error = ErrorFactory.createString("QueryError", s"deviceId not found: deviceId=$deviceId, from=$fromOpt, size=$sizeOpt")
    HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))
  }

  private def errorResponseMessage(deviceMessage: DeviceDataRaw): HttpResponse = {
    val error = ErrorFactory.createString("CreateError", s"failed to persist message")
    HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))
  }

}
