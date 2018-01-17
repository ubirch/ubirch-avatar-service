package com.ubirch.avatar.backend.actor

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceInfo
import com.ubirch.avatar.util.server.AvatarSession
import com.ubirch.user.client.rest.UserServiceClientRest
import com.ubirch.user.model.rest.Group
import com.ubirch.util.model.JsonErrorResponse

import akka.actor.Actor
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * Created by derMicha on 30/10/16.
  */
case class AllDevices(session: AvatarSession)

case class AllStubs(session: AvatarSession)

case class CreateDevice(session: AvatarSession, device: Device)

case class CreateResult(error: Option[JsonErrorResponse] = None,
                        device: Option[Device] = None
                       )

case class AllDevicesResult(devices: Seq[Device])

case class AllStubsResult(stubs: Seq[DeviceInfo])

class DeviceApiActor(implicit httpClient: HttpExt, materializer: Materializer) extends Actor with StrictLogging {

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
    logger.debug("AllStubs")
    queryGroups(session) flatMap { g =>
      logger.debug(s"allStubs groups: $g")
      DeviceManager.allStubs(g)
    }
  }

  private def createDevice(session: AvatarSession, deviceInput: Device): Future[CreateResult] = {

    DeviceManager.info(deviceInput.deviceId).flatMap {

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

        addGroupsAndOwner(session, deviceInput) flatMap {

          case None =>
            logger.error(s"createDevice(): failed to prepare device creation: device.hwDeviceId=${deviceInput.hwDeviceId}, userContext=${session.userContext}")
            Future(
              CreateResult(
                error = Some(
                  JsonErrorResponse(
                    errorType = "CreationError",
                    errorMessage = "failed to prepare device creation"
                  )
                )
              )
            )

          case Some(deviceToCreate: db.device.Device) =>

            logger.debug(s"creating: db.device.Device=$deviceToCreate")
            DeviceManager.create(deviceToCreate).map {

              case None =>
                logger.error(s"createDevice(): failed to create device: ${deviceInput.hwDeviceId}")
                CreateResult(
                  error = Some(
                    JsonErrorResponse(
                      errorType = "CreationError",
                      errorMessage = "failed to save device to database"
                    )
                  )
                )

              case Some(deviceCreated: db.device.Device) =>
                logger.debug("convert db.device.Device to rest.device.Device")
                CreateResult(device = Some(deviceCreated))

            }

        }

    }

  }

  private def addGroupsAndOwner(session: AvatarSession, device: Device): Future[Option[db.device.Device]] = {

    for {

      groupIds <- queryGroups(session)
      ownerId <- queryOwnerId(session)

    } yield {

      if (ownerId.isEmpty) {

        logger.error(s"unable to create device if ownerId is missing: $device, session=$session")
        None

      } else {

        Some(
          device.copy(
            groups = groupIds,
            owners = ownerId
          )
        )

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

  private def queryOwnerId(session: AvatarSession): Future[Set[UUID]] = {

    UserServiceClientRest.userGET(
      providerId = session.userContext.providerId,
      externalUserId = session.userContext.userId
    ) map {

      case None =>
        logger.debug(s"queryOwnerId: missing user record (session=$session)")
        Set.empty

      case Some(user) if user.id.isDefined =>
        logger.debug(s"queryOwnerId: ${user.id} (session=$session)")
        Set(user.id.get)

      case Some(user) if user.id.isEmpty =>
        logger.debug(s"queryOwnerId: user without id (user=$user, session=$session)")
        Set()

    }

  }

}
