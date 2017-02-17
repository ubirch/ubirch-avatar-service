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

        } ~ pathPrefix(byDate) {

          path(from / Segment / to / Segment) { (fromString, toString) =>
            complete(s"OK: from=$fromString, to=$toString") // TODO call DeviceDataProcessedManager
          } ~ path(before / Segment) { beforeString =>
            complete(s"OK: before=$beforeString") // TODO call DeviceDataProcessedManager
          } ~ path(after / Segment) { afterString =>
            complete(s"OK: after=$afterString") // TODO call DeviceDataProcessedManager
          } ~ path(day / Segment) { dayString =>
            complete(s"OK: day=$dayString") // TODO call DeviceDataProcessedManager
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

      case Some(fromInt) =>
        sizeOpt match {
          case Some(size) => DeviceDataProcessedManager.history(deviceId.toString, fromInt, size)
          case None => DeviceDataProcessedManager.history(deviceId.toString, fromInt)
        }

      case None => DeviceDataProcessedManager.history(deviceId.toString)

    }

  }

}
