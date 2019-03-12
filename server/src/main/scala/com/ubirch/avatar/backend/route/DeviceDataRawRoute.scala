package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.core.actor.{DeviceRawDataReprocessing, DeviceRawDataReprocessingActor}
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-11-02
  */
class DeviceDataRawRoute(implicit system: ActorSystem) extends ResponseUtil
  with CORSDirective {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val reprocessingActor = system.actorOf(DeviceRawDataReprocessingActor.props, ActorNames.REPROCESSING)

  val route: Route = {

    // TODO authentication

    path("data" / "reprocess" / Segment) { dayStr =>
      try {
        val day = new DateTime(dayStr, DateTimeZone.UTC)
        reprocessingActor ! DeviceRawDataReprocessing(day)
        complete(s"$day")
      }
      catch {
        case e: Exception =>
          complete(serverErrorResponse(errorType = "validationError", errorMessage = s"could not parse day: $dayStr"))
      }

    } ~ path(data / raw) {
      post {
        respondWithCORS {
          entity(as[DeviceDataRaw]) { deviceMessage =>

            onComplete(DeviceDataRawManager.store(deviceMessage)) {

              case Success(res) => res match {
                case None =>
                  complete(requestErrorResponse("CreateError", s"failed persist message: $deviceMessage"))
                case Some(storedMessage) =>
                  complete(storedMessage)
              }

              case Failure(t) =>
                complete(requestErrorResponse("CreateError", s"failed persist message: $deviceMessage"))

            }

          }
        }
      }
    }

  }

}
