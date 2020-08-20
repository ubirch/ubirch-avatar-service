package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.HttpExt
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import com.ubirch.avatar.config.{Config, ConfigKeys}
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.transformer.model.MessageReceiver
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import org.apache.commons.codec.binary.Hex

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * Created by derMicha on 28/10/16.
  * This Actor checks incoming messages
  */
class MessageValidatorActor(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  implicit val actorSystem: ActorSystem = context.system
  private val processorActor = context
    .actorSelection(ActorNames.MSG_PROCESSOR_PATH)

  private val replayFilterActor = context
    .actorSelection(ActorNames.REPLAY_FILTER_PATH)

  private val outboxManagerActor = context
    .actorSelection(ActorNames.DEVICE_OUTBOX_MANAGER_PATH)

  override def receive: Receive = {

    case drd: DeviceDataRaw if drd.v == MessageVersion.v000 =>
      val s = sender()

      log.debug(s"received message with version ${drd.v}")

      DeviceCoreUtil.validateSimpleMessage(hashedHwDeviceId = drd.a).map {
        case Some(dev) =>
          processorActor tell((drd, dev), sender = s)
        case None =>
          s ! logAndCreateErrorResponse(errType = "ValidationError", msg = s"invalid hwDeviceId: ${drd.a}", deviceId = None, hashedHwDeviceId = Some(drd.a))
      }.recover {
        case t: Throwable =>
          log.error(t, "unable to validate message")
          s ! logAndCreateErrorResponse(errType = "ValidationError", msg = s"unknown error: ${t.getMessage}", deviceId = None, hashedHwDeviceId = Some(drd.a))
      }

    case drd: DeviceDataRaw if drd.v == MessageVersion.v001 =>
      val s = sender()

      log.debug(s"received message with version ${drd.v}")

      DeviceCoreUtil.validateMessage(hashedHwDeviceId = drd.a, authToken = drd.s.getOrElse("nosignature"), payload = drd.p).map {
        case Some(dev) =>
          processorActor tell((drd, dev), sender = s)
        case None =>
          s ! logAndCreateErrorResponse(errType = "ValidationError", msg = s"invalid simple signature: ${drd.a} / ${drd.s}", deviceId = None, hashedHwDeviceId = Some(drd.a))
      }

    case drd: DeviceDataRaw if drd.v == MessageVersion.v002 || drd.v == MessageVersion.v003 =>
      val s = sender()

      log.debug(s"received message version: ${drd.v}")

      val hashedPayload = if (drd.umv.isDefined && drd.umv.get == 1) true else false

      DeviceManager.infoByHashedHwId(drd.a).map {
        case Some(dev) =>
          if (drd.s.isDefined) {
            (if (drd.k.isEmpty && drd.mppay.isEmpty) {
              DeviceCoreUtil.validateSignedMessage(device = dev, signature = drd.s.get, payload = drd.p, hashedPayload = hashedPayload)
            }
            else if (drd.k.isEmpty && drd.mppay.isDefined) {
              val mp = Hex.decodeHex(drd.mppay.get.toCharArray)
              DeviceCoreUtil.validateSignedMessage(device = dev, signature = drd.s.get, payload = mp, hashedPayload = hashedPayload)
            }
            else if (drd.k.isDefined && drd.mppay.isEmpty)
              DeviceCoreUtil.validateSignedMessageWithKey(key = drd.k.get, signature = drd.s.get, payload = drd.p)
            else if (drd.k.isDefined && drd.mppay.isDefined) {
              val mp = Hex.decodeHex(drd.mppay.get.toCharArray)
              DeviceCoreUtil.validateSignedMessageWithKey(key = drd.k.get, signature = drd.s.get, payload = mp)
            }
            else
              Future(false)) map {
              case true =>
                replayFilterActor tell((drd, dev), sender = s)
              case _ =>
                s ! logAndCreateErrorResponse(s"ecc signature check failed: ${drd.s.getOrElse("no signature")} (hwDeviceId: ${drd.a})", "ValidationError", deviceId = Some(dev.deviceId), hashedHwDeviceId = Some(drd.a))
            }
          }
          else
            s ! logAndCreateErrorResponse(s"signature missing: ${drd.a}}", "ValidationError", deviceId = Some(dev.deviceId), hashedHwDeviceId = Some(dev.hashedHwDeviceId))

        case None =>
          s ! logAndCreateErrorResponse(s"invalid hashedHwDeviceId: ${drd.a}", "ValidationError", deviceId = None, hashedHwDeviceId = Some(drd.a))
      }

    case drd: DeviceDataRaw if drd.v == MessageVersion.v40 =>
      val s = sender()

      log.debug(s"received message version: ${drd.v}")

      DeviceManager.infoByHashedHwId(drd.a).map {
        case Some(dev) =>
          processorActor tell((drd, dev), sender = s)
        case None =>
          s ! logAndCreateErrorResponse(s"invalid hashedHwDeviceId: ${drd.a}", "ValidationError", deviceId = None, hashedHwDeviceId = Some(drd.a))
      }

    case drd: DeviceDataRaw =>
      sender ! logAndCreateErrorResponse(s"received unknown message version: ${drd.v}", "ValidationError", deviceId = None, hashedHwDeviceId = Some(drd.a))
    case _ =>
      sender ! logAndCreateErrorResponse(msg = "received unknown message", errType = "ValidationError", deviceId = None, hashedHwDeviceId = None)
  }

  override def unhandled(message: Any): Unit = {
    log.error(s"received unknown message: ${message.toString} (${message.getClass.toGenericString}) from: ${context.sender()}")
  }


  private def logAndCreateErrorResponse(msg: String, errType: String, deviceId: Option[String], hashedHwDeviceId: Option[String]): JsonErrorResponse = {
    log.error(msg)
    val jer = JsonErrorResponse(errorType = errType, errorMessage = msg)
    if (deviceId.isDefined)
      outboxManagerActor ! MessageReceiver(deviceId.get, jer.toJsonString, ConfigKeys.DEVICEOUTBOX)
    jer
  }

}

object MessageValidatorActor {
  def props()(implicit mongo: MongoUtil,
              httpClient: HttpExt,
              materializer: Materializer): Props = new RoundRobinPool(
    Config.akkaNumberOfFrontendWorkers).props(
    Props(new MessageValidatorActor()))
}