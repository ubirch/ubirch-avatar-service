package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.core.actor.{DeviceRawDataReprocessing, DeviceRawDataReprocessingActor}
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants._
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.ExecutionContextExecutor

/**
  * author: cvandrei
  * since: 2016-11-02
  */
class DeviceDataRawRoute(implicit httpClient: HttpExt, materializer: Materializer, system: ActorSystem) extends ResponseUtil
  with CORSDirective
  with StrictLogging {


  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
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

            DeviceDataRawManager.store(deviceMessage) match {

              case None =>
                complete(requestErrorResponse("CreateError", s"failed persist message: $deviceMessage"))
              case Some(storedMessage) =>
                complete(storedMessage)
            }
          }
        }
      }
    } ~ path(data / transferDates / Segment) { deviceId =>
      oidcDirective.oidcToken2UserContext { userContext =>
        get {
          logger.error(s"Disabled endpoint GET /data/transferDates/$deviceId was called by ${userContext.userId}")
          respondWithCORS {
            complete(requestErrorResponse(errorType = "Disabled endpoint error", errorMessage =
              "this endpoint has been disabled together with TrackleService's endpoint /device/statistics/create"))
            //            onComplete(DeviceDataRawManager.getTransferDates(deviceId)) {
            //              case Success(dates: Set[DateTime]) =>
            //                logger.info(s"the following unique transferDates will be returned: $dates")
            //                complete(dates)
            //              case Failure(t) =>
            //                complete(requestErrorResponse("GetDataTransferDatesError",
            //                  s"failed retrieve dates of data transfers for heDeviceId: $deviceId due to: $t"))
            //            }
          }
        }
      }
    }
  }
  private val reprocessingActor = system.actorOf(DeviceRawDataReprocessingActor.props, ActorNames.REPROCESSING)
  private val oidcDirective = new OidcDirective()

}
