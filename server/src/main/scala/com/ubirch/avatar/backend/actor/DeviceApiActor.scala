package com.ubirch.avatar.backend.actor

import java.util.UUID

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
import play.api.libs.ws.WSClient

import scala.concurrent.Future

/**
  * Created by derMicha on 30/10/16.
  */

case class CreateDevice(session: AvatarSession, device: Device)

class DeviceApiActor(implicit ws: WSClient) extends Actor with StrictLogging {

  import context.dispatcher

  override def receive: Receive = {

    case cd: CreateDevice =>
      val from = sender
      createDevice(cd.session, cd.device) map (from ! _)

    case _ =>
      logger.error("received unknown message")
      sender() ! JsonErrorResponse(errorType = "ServerError", errorMessage = "internal server error")

  }

  private def createDevice(session: AvatarSession, device: Device) = {

    DeviceManager.info(device.deviceId).map {

      case Some(dev) =>
        JsonErrorResponse(
          errorType = "CreationError",
          errorMessage = s"device already exist: $dev"
        )

      case None =>

        addGroup(session, device) map {

          case None =>
            JsonErrorResponse(
              errorType = "CreationError",
              errorMessage = s"failed to find groups to add to the device: ${device.deviceId}"
            )

          case Some(dbDevice: db.device.Device) =>

            logger.debug(s"creating: db.device.Device=$dbDevice")
            DeviceManager.create(dbDevice).map {

              case None =>
                JsonErrorResponse(
                  errorType = "CreationError",
                  errorMessage = s"failed to create device: ${device.deviceId}"
                )

              case Some(deviceObject) => deviceObject

            }

        }

    }

  }

  private def addGroup(session: AvatarSession, device: Device): Future[Option[db.device.Device]] = {

    val groupIdsFuture: Future[Option[Set[UUID]]] = UserServiceClientRest.groups(
      contextName = session.userContext.context,
      providerId = session.userContext.providerId,
      externalUserId = session.userContext.userId
    ) map {

      case None => None

      case Some(groups: Set[Group]) =>
        val groupIds = groups map (_.id.get)
        Some(groupIds)

    }

    groupIdsFuture map {

      case None =>
        logger.debug(s"groups found: None (userContext=${session.userContext})")
        None

      case Some(groups: Set[UUID]) =>
        val dbDevice = Json4sUtil.any2any[db.device.Device](device).copy(groups = groups)
        logger.debug(s"groups found and added to Device: groups=$groups, device=$dbDevice (userContext=${session.userContext})")
        Some(dbDevice)

    }

  }

}
