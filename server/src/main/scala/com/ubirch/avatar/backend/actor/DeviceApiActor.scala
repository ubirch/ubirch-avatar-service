package com.ubirch.avatar.backend.actor

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.rest.device.Device
import com.ubirch.avatar.util.server.AvatarSession
import com.ubirch.user.client.rest.UserServiceClientRest
import com.ubirch.user.model.rest.Group
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.model.JsonErrorResponse

import akka.actor.Actor
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * Created by derMicha on 30/10/16.
  */
case class CreateDevice(session: AvatarSession, device: Device)

case class CreateResult(error: Option[JsonErrorResponse] = None,
                        device: Option[rest.device.Device] = None
                       )

class DeviceApiActor(implicit ws: StandaloneWSClient) extends Actor with StrictLogging {

  implicit protected val executionContext: ExecutionContextExecutor = context.system.dispatcher
  //implicit protected val materializer = ActorMaterializer()

  override def receive: Receive = {

    case cd: CreateDevice =>
      val from = sender
      createDevice(cd.session, cd.device) map (from ! _)

    case _ =>
      logger.error("received unknown message")
      sender() ! JsonErrorResponse(errorType = "ServerError", errorMessage = "internal server error")

  }

  private def createDevice(session: AvatarSession, device: Device): Future[CreateResult] = {

    DeviceManager.info(device.deviceId).flatMap {

      case Some(dev) =>
        logger.error(s"createDevice(): device already exists: ${dev.deviceId}")
        Future(
          CreateResult(
            error = Some(
              JsonErrorResponse(
                errorType = "CreationError",
                errorMessage = s"device already exist: $dev"
              )
            )
          )
        )

      case None =>

        logger.debug(s"createDevice(): device does not exist yet...creating it now: ${device.hwDeviceId}")
        addGroup(session, device) flatMap {

          case None =>
            logger.error(s"createDevice(): failed to add groups to device: ${device.hwDeviceId}")
            Future(
              CreateResult(
                error = Some(
                  JsonErrorResponse(
                    errorType = "CreationError",
                    errorMessage = s"failed to find groups to add to the device: ${device.deviceId}"
                  )
                )
              )
            )

          case Some(dbDevice: db.device.Device) =>

            logger.debug(s"creating: db.device.Device=$dbDevice")
            DeviceManager.create(dbDevice).map {

              case None =>
                logger.error(s"createDevice(): failed to create device: ${device.hwDeviceId}")
                CreateResult(
                  error = Some(
                    JsonErrorResponse(
                      errorType = "CreationError",
                      errorMessage = s"failed to create device: ${device.deviceId}"
                    )
                  )
                )

              case Some(deviceObject: db.device.Device) =>
                logger.debug("convert db.device.Device to rest.device.Device")
                val restDevice = Json4sUtil.any2any[Device](deviceObject)
                CreateResult(device = Some(restDevice))

            }

        }

    }

  }

  private def addGroup(session: AvatarSession, device: Device): Future[Option[db.device.Device]] = {

    logger.debug("addGroup(); query user-service for groups")
    UserServiceClientRest.groups(
      contextName = session.userContext.context,
      providerId = session.userContext.providerId,
      externalUserId = session.userContext.userId
    ) map {

      case None =>
        logger.debug("addGroup(): found None")
        None

      case Some(groups: Set[Group]) =>

        logger.debug(s"addGroup(): found $groups")
        val groupIds = groups map (_.id.get)

        if (groupIds.isEmpty) {

          logger.error(s"failed to add missing groups to device: userContext=${session.userContext}")
          None

        } else {

          val dbDevice = Json4sUtil.any2any[db.device.Device](device).copy(groups = groupIds)
          logger.debug(s"addGroup(); groups found and added to Device: groups=$groups, device=$dbDevice (userContext=${session.userContext})")
          Some(dbDevice)

        }

    }

  }

}
