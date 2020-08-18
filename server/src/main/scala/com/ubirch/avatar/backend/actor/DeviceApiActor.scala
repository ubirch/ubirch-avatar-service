package com.ubirch.avatar.backend.actor

import java.util.UUID

import akka.actor.{Actor, Props}
import akka.http.scaladsl.HttpExt
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{DeviceInfo, DeviceUserClaim, DeviceUserClaimRequest}
import com.ubirch.avatar.util.server.AvatarSession
import com.ubirch.user.client.rest.UserServiceClientRest
import com.ubirch.user.model.rest.Group
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil

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

class DeviceApiActor(implicit mongo: MongoUtil,
                     httpClient: HttpExt,
                     materializer: Materializer) extends Actor with StrictLogging {

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

    case duc: DeviceUserClaimRequest =>
      val s = sender

      DeviceManager.infoByHwId(duc.hwDeviceId).map {
        case Some(device) =>
          logger.debug(s"found device information $device to claim device with hwDeviceId ${duc.hwDeviceId}.")
          val userId = duc.userId
          if (device.owners.isEmpty || device.owners.contains(userId)) {
            if (device.owners.isEmpty) {
              logger.info(s"updating device with userId $userId as new owner.")
              DeviceManager.update(device.copy(owners = Set(userId)))
            } else {
              logger.info(s"user with id $userId is already owner of device $device")
            }
            s ! DeviceUserClaim(
              hwDeviceId = duc.hwDeviceId,
              deviceId = device.deviceId,
              userId = userId
            )
          } else {
            val errorMsg = s"device ${duc.hwDeviceId} already claimed by ${device.owners} user(s) and not by $userId"
            logger.error(s"error claiming device $errorMsg")
            s ! JsonErrorResponse(errorType = "DeviceClaimError", errorMessage = errorMsg)
          }

        case None =>
          logger.error(s"couldn't find device information to claim device with hwDeviceId ${duc.hwDeviceId}")
          s ! JsonErrorResponse(
            errorType = "DeviceClaimError",
            errorMessage = s"device ${duc.hwDeviceId} does not exist"
          )
      }
  }

  override def unhandled(message: Any): Unit = {
    context.sender ! JsonErrorResponse(
      errorType = "InternalError",
      errorMessage = s"received unknown message: ${message.toString}")
  }

  private def allDevices(session: AvatarSession): Future[Seq[Device]] = {

    queryGroups(session).flatMap(groups => DeviceManager.all(session.userContext.userId, groups))

  }

  private def allStubs(session: AvatarSession): Future[Seq[DeviceInfo]] = {
    logger.debug("AllStubs")

    queryGroups(session) flatMap { g =>
      logger.debug(s"allStubs groups: $g")
      DeviceManager.allStubs(userId = session.userContext.userId, groups = g)
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

          case Some(deviceToCreate: Device) =>

            logger.debug(s"creating: db.device.Device=$deviceToCreate")
            DeviceManager.create(deviceToCreate).map {

              case None =>
                logger.error(s"createDevice(): failed to create device: ${
                  deviceInput.hwDeviceId
                }")
                CreateResult(
                  error = Some(
                    JsonErrorResponse(
                      errorType = "CreationError",
                      errorMessage = "failed to save device to database"
                    )
                  )
                )

              case Some(deviceCreated: Device) =>
                logger.debug("convert db.device.Device to rest.device.Device")
                CreateResult(device = Some(deviceCreated))

            }

        }

    }

  }

  private def addGroupsAndOwner(session: AvatarSession, device: Device): Future[Option[Device]] = {

    queryGroups(session).map { groupIds =>

      val ownerId = Set(session.userContext.userId)
      Some(
        device.copy(
          groups = groupIds ++ device.groups,
          owners = ownerId
        )
      )
    }
  }

  private def queryGroups(session: AvatarSession): Future[Set[UUID]] = {

    logger.debug(s"contextName = ${session.userContext.context} / providerId = ${session.userContext.providerId} / externalUserId = ${session.userContext.externalUserId}")

    UserServiceClientRest.groupMemberOf(
      contextName = session.userContext.context,
      providerId = session.userContext.providerId,
      externalUserId = session.userContext.externalUserId
    ) map {

      case None =>
        logger.debug("queryGroups: None")
        Set[UUID]()

      case Some(groups: Set[Group]) =>
        logger.debug(s"found groups: groups=$groups, userContext=${session.userContext}")
        groups filter (_.id.isDefined) map (_.id.get)
    }
  }


}

object DeviceApiActor {
  def props()(implicit mongo: MongoUtil,
              httpClient: HttpExt,
              materializer: Materializer): Props = new RoundRobinPool(
    Config.akkaNumberOfFrontendWorkers)
    .props(Props(new DeviceApiActor()))
}