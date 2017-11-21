package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceStateUpdate}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.avatar.util.server.RouteConstants.update
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Created by derMicha on 26/02/17.
  */
class DeviceUpdatePlainRoute(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer, system: ActorSystem, allCounter: AllCounter)
  extends MyJsonProtocol
    with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val validatorActor = system.actorSelection(ActorNames.MSG_VALIDATOR_PATH)

  val route: Route = {
    path(update) {
      pathEnd {
        post {
          entity(as[String]) { ddrString =>
            Json4sUtil.string2JValue(ddrString) match {
              case Some(ddrJson) =>
                ddrJson.extractOpt[DeviceDataRaw] match {
                  case Some(ddr) =>
                    onComplete(validatorActor ? ddr) {
                      case Success(resp) =>
                        resp match {
                          case dm: DeviceStateUpdate =>
                            val dsuJson = Json4sUtil.any2jvalue(dm).get
                            val dsuString = Json4sUtil.jvalue2String(dsuJson)
                            allCounter.requests.inc
                            complete(dsuString)
                          case _ =>
                            allCounter.requestsErrors.inc
                            logger.error("update device data failed")
                            complete("NOK: DeviceStateUpdate failed")
                        }
                      case Failure(t) =>
                        allCounter.requestsErrors.inc
                        logger.error("update device data failed", t)
                        complete("NOK: internal Server Error")
                    }
                  case None =>
                    allCounter.requestsErrors.inc
                    complete("NOK: wrong json")
                }

              case None =>
                allCounter.requestsErrors.inc
                complete("NOK: invalid json input")
            }
          }
        }
      }
    }
  }
}
