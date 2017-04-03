package com.ubirch.avatar.backend.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.avatar.core.device.DeviceTypeManager
import com.ubirch.avatar.model.device.DeviceType
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.oidc.directive.OidcDirective
import com.ubirch.util.redis.RedisClientUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import redis.RedisClient

import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2016-11-09
  */
trait DeviceTypeRoute extends CORSDirective
  with MyJsonProtocol
  with ResponseUtil
  with StrictLogging {

  implicit val system = ActorSystem()

  private val configPrefix = ConfigKeys.prefix
  private val redis: RedisClient = RedisClientUtil.newInstance(configPrefix)(system)
  private val oidcDirective = new OidcDirective(configPrefix = configPrefix, redis = redis)

  val route: Route = {

    path(RouteConstants.deviceType) {
      respondWithCORS {

        get {
          oidcDirective.oidcToken2UserContext { userContext =>
            onComplete(DeviceTypeManager.all()) {

              case Success(res) =>
                complete(res)

              case Failure(t) =>
                logger.error(s"failed to query all device types", t)
                complete(serverErrorResponse("QueryError", errorMessage = t.getMessage))

            }
          }

        } ~ post {
          entity(as[DeviceType]) { postDeviceType =>
            onComplete(DeviceTypeManager.create(postDeviceType)) {

              case Success(resp) => resp match {
                case Some(deviceType) => complete(deviceType)
                case None => complete(requestErrorResponse("CreateError", s"another deviceType with key=${postDeviceType.key} already exists or otherwise something else on the server went wrong"))
              }

              case Failure(t) =>
                logger.error(s"deviceType creation failed: deviceType=$postDeviceType", t)
                complete(serverErrorResponse(errorType = "CreateError", errorMessage = t.getMessage))
            }
          }

        } ~ put {
          entity(as[DeviceType]) { postDeviceType =>
            onComplete(DeviceTypeManager.update(postDeviceType)) {

              case Success(resp) => resp match {
                case Some(deviceType) => complete(deviceType)
                case None => complete(requestErrorResponse("UpdateError", s"no deviceType with key=${postDeviceType.key} exists or otherwise something else on the server went wrong"))
              }

              case Failure(t) =>
                logger.error(s"deviceType update failed: deviceType=$postDeviceType", t)
                complete(serverErrorResponse(errorType = "UpdateError", errorMessage = t.getMessage))

            }
          }

        }
      }
    } ~ path(RouteConstants.deviceType / RouteConstants.init) {
      respondWithCORS {
        oidcDirective.oidcToken2UserContext { userContext =>
          get {
            onComplete(DeviceTypeManager.init()) {

              case Success(resp) => complete(resp)

              case Failure(t) =>
                logger.error("failed to create default deviceTypes", t)
                complete(serverErrorResponse(errorType = "CreationError", errorMessage = t.getMessage))

            }
          }
        }
      }
    }
  }

}
