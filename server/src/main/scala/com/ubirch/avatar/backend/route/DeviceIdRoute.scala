package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceIdRoute(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer, system: ActorSystem)
  extends ResponseUtil
    with CORSDirective
    with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)
  val route: Route = {
    path(JavaUUID) { deviceId =>

      respondWithCORS {

        get {
          //Not being used anymore after restructuring of EOL flag communication
          logger.error(s"Disabled endpoint GET /device/<deviceId> with id $deviceId was called")
          complete(requestErrorResponse(errorType = "Disabled endpoint error", errorMessage = "this endpoint has been disabled"))

        } ~ post {
          entity(as[Device]) { device =>
            logger.error(s"Disabled endpoint POST /device/<deviceId> with id $deviceId and device $device was called")
            complete(requestErrorResponse(errorType = "Disabled endpoint error", errorMessage = "this endpoint has been disabled"))
          }

        } ~ put {
          logger.error(s"Disabled endpoint PUT /device/id with id $deviceId was called")
          complete(requestErrorResponse(errorType = "Disabled endpoint error", errorMessage = "this endpoint has been disabled"))

        } ~ delete {
          logger.error(s"Disabled endpoint GET /device/id with id $deviceId was called")
          complete(requestErrorResponse(errorType = "Disabled endpoint error", errorMessage =
            "this endpoint has been disabled together with TrackleService's endpoint DELETE /user by userContext"))
        }
      }
    }
  }



}

case class DeviceIdResult(device: Option[Device] = None,
                          deviceDeleted: Boolean = false,
                          error: Option[HttpResponse] = None
                         )