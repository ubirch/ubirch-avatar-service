package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.services.util.DeviceCoreUtil
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

  private val processorActor = context
    .actorOf(MessageProcessorActor.props(), ActorNames.MSG_PROCESSOR)

  private val replayFilterActor = context
    .actorOf(ReplayFilterActor.props(), ActorNames.REPLAY_FILTER)

  override def receive: Receive = {

    case drd: DeviceDataRaw if drd.v == MessageVersion.v000 =>
      val s = sender()

      log.debug(s"received message with version ${drd.v}")

      DeviceCoreUtil.validateSimpleMessage(hwDeviceId = drd.a).map {
        case Some(dev) =>
          processorActor tell((drd, dev), sender = s)
        case None =>
          s ! logAndCreateErrorResponse(errType = "ValidationError", msg = s"invalid hwDeviceId: ${drd.a}")
      }

    case drd: DeviceDataRaw if drd.v == MessageVersion.v001 =>
      val s = sender()

      log.debug(s"received message with version ${drd.v}")

      DeviceCoreUtil.validateMessage(hashedHwDeviceId = drd.a, authToken = drd.s.getOrElse("nosignature"), payload = drd.p).map {
        case Some(dev) =>
          processorActor tell((drd, dev), sender = s)
        case None =>
          s ! logAndCreateErrorResponse(errType = "ValidationError", msg = s"invalid simple signature: ${drd.a} / ${drd.s}")
      }

    case drd: DeviceDataRaw if drd.v == MessageVersion.v002 || drd.v == MessageVersion.v003 =>
      val s = sender()

      log.debug(s"received message version: ${drd.v}")

      DeviceManager.infoByHashedHwId(drd.a).map {
        case Some(dev) =>
          if (drd.s.isDefined) {
            (if (drd.k.isEmpty && drd.mpraw.isEmpty) {
              DeviceCoreUtil.validateSignedMessage(device = dev, signature = drd.s.get, payload = drd.p)
            }
            else if (drd.k.isEmpty && drd.mpraw.isDefined) {
              val mp = Hex.decodeHex(drd.mpraw.get.toString.toCharArray)
              DeviceCoreUtil.validateSignedMessage(device = dev, signature = drd.s.get, payload = mp)
            }
            else if (drd.k.isDefined && drd.mpraw.isEmpty)
              DeviceCoreUtil.validateSignedMessage(key = drd.k.get, signature = drd.s.get, payload = drd.p)
            else if (drd.k.isDefined && drd.mpraw.isDefined) {
              val mp = Hex.decodeHex(drd.mpraw.get.toString.toCharArray)
              DeviceCoreUtil.validateSignedMessage(key = drd.k.get, signature = drd.s.get, payload = mp)
            }
            else
              Future(false)) map {
              case true =>
                //                processorActor tell((drd, dev), sender = s)
                replayFilterActor tell((drd, dev), sender = s)
              case _ =>
                s ! logAndCreateErrorResponse(s"invalid ecc signature: ${drd.a} / ${drd.s} (${drd.k.getOrElse("without pubKey")})", "ValidationError")
            }
          }
          else
            s ! logAndCreateErrorResponse(s"signature missing: ${drd.a}}", "ValidationError")

        case None =>
          s ! logAndCreateErrorResponse(s"invalid hwDeviceId: ${drd.a}", "ValidationError")
      }

    case drd: DeviceDataRaw if drd.v == MessageVersion.v40 =>
      val s = sender()

      log.debug(s"received message version: ${drd.v}")

      DeviceManager.infoByHashedHwId(drd.a).map {
        case Some(dev) =>
          processorActor tell((drd, dev), sender = s)
        case None =>
          s ! logAndCreateErrorResponse(s"invalid hwDeviceId: ${drd.a}", "ValidationError")
      }

    case drd: DeviceDataRaw =>
      sender ! logAndCreateErrorResponse(s"received unknown message version: ${drd.v}", "ValidationError")
    case _ =>
      sender ! logAndCreateErrorResponse("received unknown message", "ValidationError")
  }

  private def logAndCreateErrorResponse(msg: String, errType: String): JsonErrorResponse = {
    log.error(msg)
    JsonErrorResponse(errorType = errType, errorMessage = msg)
  }

}
