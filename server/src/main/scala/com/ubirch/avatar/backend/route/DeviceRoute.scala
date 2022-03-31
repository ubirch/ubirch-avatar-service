package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceRoute(implicit httpClient: HttpExt, materializer: Materializer, system: ActorSystem)
  extends ResponseUtil
    with CORSDirective
    with StrictLogging
    with RouteAnalyzingByLogsSupport {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)


  val route: Route =
    respondWithCORS {

        get {
          //Not being used, as EOL update functionality was refactored
          logger.error("Disabled Endpoint GET /device by userContext was called, though EOL update was refactored")
          complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))

        } ~ post {

          entity(as[Device]) { device =>

            logger.error(s"Disabled Endpoint POST /device with device $device was called")
            complete(requestErrorResponse("Disabled Endpoint", "this endpoint was disabled"))

          }


      }
    }

}
