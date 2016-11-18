package com.ubirch.avatar.backend.route

import java.util.UUID

import com.ubirch.avatar.backend.ResponseUtil
import com.ubirch.avatar.core.device.DeviceDataProcessedManager
import com.ubirch.avatar.model.device.DeviceDataProcessed
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-30
  */
trait DeviceDataHistoryRoute extends MyJsonProtocol
  with CORSDirective
  with ResponseUtil {

  implicit val system = ActorSystem()

  import system.dispatcher

  val route: Route = {

    // TODO authentication

    pathPrefix(JavaUUID / data) { deviceId =>

      path(history) {
        respondWithCORS {
          get {
            onSuccess(queryHistory(deviceId)) {
              case None => complete(errorResponseHistory(deviceId))
              case Some(deviceData) => complete(deviceData)
            }
          }
        }

      } ~ pathPrefix(history) {

        path(IntNumber) { from =>
          respondWithCORS {
            get {
              onSuccess(queryHistory(deviceId, Some(from))) {
                case None => complete(errorResponseHistory(deviceId, Some(from)))
                case Some(deviceData) => complete(deviceData)
              }
            }
          }

        } ~ path(IntNumber / IntNumber) { (from, size) =>
          respondWithCORS {
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

  //TODO refactor this, put it into an actor
  private def queryHistory(deviceId: UUID,
                           fromOpt: Option[Int] = None,
                           sizeOpt: Option[Int] = None
                          ): Future[Option[Seq[DeviceDataProcessed]]] = {

    val deviceData: Future[Seq[DeviceDataProcessed]] = fromOpt match {

      case Some(from) =>
        sizeOpt match {
          case Some(size) => DeviceDataProcessedManager.history(deviceId.toString, from, size)
          case None => DeviceDataProcessedManager.history(deviceId.toString, from)
        }

      case None => DeviceDataProcessedManager.history(deviceId.toString)

    }

    deviceData map {
      case seq if seq.isEmpty => None
      case seq => Some(seq)
    }

  }

  private def errorResponseHistory(deviceId: UUID,
                                   fromOpt: Option[Long] = None,
                                   sizeOpt: Option[Long] = None
                                  ): HttpResponse = {
    requestErrorResponse("QueryError", s"deviceId not found: deviceId=$deviceId, from=$fromOpt, size=$sizeOpt")
  }
}
