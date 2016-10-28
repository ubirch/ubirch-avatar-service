package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.core.device.DeviceMessageManager
import com.ubirch.avatar.core.server.util.RouteConstants._
import com.ubirch.avatar.model.device.DeviceMessage
import com.ubirch.avatar.model.util.ErrorFactory
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-30
  */
trait DeviceMessageRoute extends MyJsonProtocol
  with CORSDirective {

  implicit val system = ActorSystem()

  import system.dispatcher

  val route: Route = {

    // TODO authentication
    respondWithCORS {

      pathPrefix(device / Segment) { deviceId =>

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

      } ~ path(device / history) {

        post {
          entity(as[DeviceMessage]) { deviceMessage =>
            onSuccess(DeviceMessageManager.store(deviceMessage)) {
              case None => complete(errorResponseMessage(deviceMessage))
              case Some(storedMessage) => complete(storedMessage)
            }

          }
        }

      }

    }

  }

  private def queryHistory(deviceId: String,
                           fromOpt: Option[Int] = None,
                           sizeOpt: Option[Int] = None
                          ): Future[Option[Seq[DeviceMessage]]] = {

    val deviceData: Future[Seq[DeviceMessage]] = fromOpt match {

      case Some(from) =>
        sizeOpt match {
          case Some(size) => DeviceMessageManager.history(deviceId, from, size)
          case None => DeviceMessageManager.history(deviceId, from)
        }

      case None => DeviceMessageManager.history(deviceId)

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

  private def errorResponseMessage(deviceMessage: DeviceMessage): HttpResponse = {
    val error = ErrorFactory.createString("CreateError", s"failed to persist message")
    HttpResponse(status = BadRequest, entity = HttpEntity(`application/json`, error))
  }

}
