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
import com.ubirch.util.json.JsonFormats
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import org.joda.time.DateTime
import org.json4s.Formats
import org.json4s.JsonAST.JField

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Try

/**
  * Created by derMicha on 30/10/16.
  */
case class CreateResult(error: Option[JsonErrorResponse] = None,
                        device: Option[Device] = None
                       )

case class AllDevicesResult(devices: Seq[Device])

case class AllStubsResult(stubs: Seq[DeviceInfo])

private class DeviceApiActor(implicit mongo: MongoUtil,
                             httpClient: HttpExt,
                             materializer: Materializer) extends Actor with StrictLogging {

  implicit protected val executionContext: ExecutionContextExecutor = context.system.dispatcher
  implicit val formats: Formats = JsonFormats.default

  override def receive: Receive = {


    case duc: DeviceUserClaimRequest =>
      val s = sender
      claimDevice(duc)
        .map(duc => s ! duc)
        .recover {
          case error: ClaimDeviceError =>
            logger.error(s"claiming device failed ${error.msg}")
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


}

object DeviceApiActor {
  def props()(implicit mongo: MongoUtil,
              httpClient: HttpExt,
              materializer: Materializer): Props = new RoundRobinPool(
    Config.akkaNumberOfFrontendWorkers)
    .props(Props(new DeviceApiActor()))
}


case class ClaimDeviceError(msg: String)
