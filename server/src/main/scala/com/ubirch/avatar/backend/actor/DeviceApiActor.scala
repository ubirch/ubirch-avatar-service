package com.ubirch.avatar.backend.actor

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.rest.device.{Device, DeviceInfo}
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
case class AllDevices(session: AvatarSession)

case class AllStubs(session: AvatarSession)

case class CreateDevice(session: AvatarSession, device: Device)

case class CreateResult(error: Option[JsonErrorResponse] = None,
                        device: Option[rest.device.Device] = None
                       )

case class AllDevicesResult(devices: Seq[Device])

case class AllStubsResult(stubs: Seq[DeviceInfo])

class DeviceApiActor(implicit ws: StandaloneWSClient) extends Actor with StrictLogging {

  implicit protected val executionContext: ExecutionContextExecutor = context.system.dispatcher

  override def receive: Receive = {

    case all: AllDevices =>
      val from = sender()
      allDevices(all.session) map (from ! AllDevicesResult(_))

    case all: AllStubs =>
      val from = sender()
      logger.debug("AllStubs")
      allStubs(all.session) map (from ! AllStubsResult(_))

    case cd: CreateDevice =>
      val from = sender
      createDevice(cd.session, cd.device) map (from ! _)

    case _ =>
      logger.error("received unknown message")
      sender() ! JsonErrorResponse(errorType = "ServerError", errorMessage = "internal server error")

  }

  private def allDevices(session: AvatarSession): Future[Seq[Device]] = {

    queryGroups(session) flatMap DeviceManager.all

  }

  private def allStubs(session: AvatarSession): Future[Seq[DeviceInfo]] = {
    logger.debug("allStubs")
    queryGroups(session) flatMap DeviceManager.allStubs

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
                errorMessage = "device already exist"
              )
            )
          )
        )

      case None =>

        addGroup(session, device) flatMap {

          case None =>
            logger.error(s"createDevice(): unable to create device if user has no groups: device.hwDeviceId=${device.hwDeviceId}, userContext=${session.userContext}")
            Future(
              CreateResult(
                error = Some(
                  JsonErrorResponse(
                    errorType = "CreationError",
                    errorMessage = "unable to create device: user has no groups"
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
                      errorMessage = "failed to create device"
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

    UserServiceClientRest.groups(
      contextName = session.userContext.context,
      providerId = session.userContext.providerId,
      externalUserId = session.userContext.userId
    ) map {

      case None => None

      case Some(groups: Set[Group]) =>

        logger.debug(s"addGroup(): found $groups")
        val groupIds = groups map (_.id.get)

        if (groupIds.isEmpty) {
          None
        } else {
          Some(Json4sUtil.any2any[db.device.Device](device).copy(groups = groupIds))
        }

    }

  }

  private def queryGroups(session: AvatarSession): Future[Set[UUID]] = {

    logger.debug(s"contextName = ${session.userContext.context} / providerId = ${session.userContext.providerId} / externalUserId = ${session.userContext.userId}")

    UserServiceClientRest.groups(
      contextName = session.userContext.context,
      providerId = session.userContext.providerId,
      externalUserId = session.userContext.userId
    ) map {

      case None =>
        logger.debug("queryGroups: None")
        Set.empty

      case Some(groups: Set[Group]) =>
        logger.debug(s"found groups: groups=$groups, userContext=${session.userContext}")
        groups filter (_.id.isDefined) map (_.id.get)
    }

  }

}
