package com.ubirch.avatar.backend.actor

import akka.actor.{Actor, Props}
import akka.http.scaladsl.HttpExt
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.{DeviceInfo, DeviceUserClaim, DeviceUserClaimRequest}
import com.ubirch.avatar.util.server.AvatarSession
import com.ubirch.user.client.UserServiceClient
import com.ubirch.user.client.model.Group
import com.ubirch.util.json.JsonFormats
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import org.joda.time.DateTime
import org.json4s.{DefaultFormats, Formats}
import org.json4s.JsonAST.JField
import org.json4s.ext.{JavaTypesSerializers, JodaTimeSerializers}

import java.util.UUID
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Try

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
  implicit val formats: Formats = JsonFormats.default

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
      claimDevice(duc)
        .map(duc => s ! duc)
        .recover {
          case error: ClaimDeviceError =>
            logger.error("claiming device failed", error)
            s ! JsonErrorResponse(
              errorType = "DeviceClaimError",
              errorMessage = error.msg
            )
        }
  }

  /**
    * Method to parse productionDate from deviceProperties.
    */
  private def parseProductionDate(device: Device): Future[Option[DateTime]] = {
    val dateOpt =
      device
        .deviceProperties
        .flatMap(_.findField {
          case JField("testTimestamp", _) => true
          case _ => false
        })
        .map(_._2.extract[String])
        .flatMap(dateString => Try(DateTime.parse(dateString)).toOption)
    Future.successful(dateOpt)
  }


  private def claimDevice(duc: DeviceUserClaimRequest): EitherT[Future, ClaimDeviceError, DeviceUserClaim] = {

    for {
      device <- EitherT.fromOptionF(DeviceManager.infoByHwId(duc.hwDeviceId),
        ClaimDeviceError(s"device wasn't found by hwDevice ${duc.hwDeviceId}"))
      parsedProductionDate <- EitherT.fromOptionF(parseProductionDate(device),
        ClaimDeviceError(s"couldn't parse field deviceProperties.testTimestamp to valid production date"))
      _ <- EitherT.fromOptionF(updateOwner(duc, device),
        ClaimDeviceError(s"device ${duc.hwDeviceId} already claimed by ${device.owners} user(s)"))
    } yield {
      DeviceUserClaim(
        duc.hwDeviceId,
        device.deviceId,
        duc.userId,
        parsedProductionDate
      )
    }
  }

  private def updateOwner(duc: DeviceUserClaimRequest, device: Device): Future[Option[Device]] = {
    val userId = duc.userId
    if (device.owners.isEmpty) {
      logger.info(s"updating device with userId $userId as new owner.")
      DeviceManager.update(device.copy(owners = Set(userId)))
    } else if (device.owners.contains(userId)) {
      logger.info(s"user with id $userId is already owner of device $device")
      Future.successful(Some(device))
    } else Future.successful(None)
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

    UserServiceClient.groupMemberOf(
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


case class ClaimDeviceError(msg: String)
