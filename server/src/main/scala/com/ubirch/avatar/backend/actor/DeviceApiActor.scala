package com.ubirch.avatar.backend.actor

import java.util.UUID

import akka.actor.{Actor, Props}
import akka.http.scaladsl.HttpExt
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import com.typesafe.scalalogging.slf4j.StrictLogging
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
          UserServiceClientRest.userGET(providerId = duc.providerId, externalUserId = duc.externalId).map {
            case Some(user) if user.id.isDefined =>
              if (device.owners.isEmpty || device.owners.contains(user.id.get)) {
                if (device.owners.isEmpty)
                  DeviceManager.update(device.copy(owners = Set(user.id.get)))
                s ! DeviceUserClaim(
                  hwDeviceId = duc.hwDeviceId,
                  deviceId = device.deviceId,
                  userId = user.id.get
                )
              }
              else {
                s ! JsonErrorResponse(
                  errorType = "DeviceClaimError",
                  errorMessage = s"device ${duc.hwDeviceId} already claimed by ${device.owners.size} user(s)"
                )
              }
            case None =>
              s ! JsonErrorResponse(
                errorType = "DeviceClaimError",
                errorMessage = s"cloud not claim device ${duc.hwDeviceId} with invalid user ${duc.externalId}"
              )
          }
        case None =>
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

    queryGroups(session) flatMap DeviceManager.all

  }

  private def allStubs(session: AvatarSession): Future[Seq[DeviceInfo]]

  = {
    logger.debug("AllStubs")
    queryOwnerId(session) flatMap { u =>
      queryGroups(session) flatMap { g =>
        logger.debug(s"allStubs groups: $g")
        val res = DeviceManager.allStubs(userId = u.head, groups = g)
        res
      }
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
            groups = groupIds ++ device.groups,
            owners = ownerId
          )
        )

      }

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
        Set.empty

      case Some(groups: Set[Group]) =>
        logger.debug(s"found groups: groups=$groups, userContext=${
          session.userContext
        }")
        groups filter (_.id.isDefined) map (_.id.get)
    }
  }

  private def queryOwnerId(session: AvatarSession): Future[Set[UUID]] = {

    UserServiceClientRest.userGET(
      providerId = session.userContext.providerId,
      externalUserId = session.userContext.externalUserId
    ) map {

      case None =>
        logger.debug(s"queryOwnerId: missing user record (session=$session)")
        Set.empty

      case Some(user) if user.id.isDefined =>
        logger.debug(s"queryOwnerId: ${
          user.id
        } (session=$session)")
        Set(user.id.get)

      case Some(user) if user.id.isEmpty =>
        logger.debug(s"queryOwnerId: user without id (user=$user, session=$session)")
        Set()

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