package com.ubirch.avatar.backend.route

import java.util.UUID

import com.ubirch.avatar.core.device.DeviceDataProcessedManager
import com.ubirch.avatar.model.device.DeviceDataProcessed
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.ActorSystem
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

  val route: Route = {

    // TODO authentication

    pathPrefix(JavaUUID / data) { deviceId =>

      path(history) {
        respondWithCORS {
          get {
            onSuccess(queryHistory(deviceId)) { deviceData =>
              complete(deviceData)
            }
          }
        }

      } ~ pathPrefix(history) {

        path(IntNumber) { from =>
          respondWithCORS {
            get {
              onSuccess(queryHistory(deviceId, Some(from))) { deviceData =>
                complete(deviceData)
              }
            }
          }

        } ~ path(IntNumber / IntNumber) { (from, size) =>
          respondWithCORS {
            get {
              onSuccess(queryHistory(deviceId, Some(from), Some(size))) { deviceData =>
                complete(deviceData)
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
                          ): Future[Seq[DeviceDataProcessed]] = {

    fromOpt match {

      case Some(from) =>
        sizeOpt match {
          case Some(size) => DeviceDataProcessedManager.history(deviceId.toString, from, size)
          case None => DeviceDataProcessedManager.history(deviceId.toString, from)
        }

      case None => DeviceDataProcessedManager.history(deviceId.toString)

    }

  }

}
