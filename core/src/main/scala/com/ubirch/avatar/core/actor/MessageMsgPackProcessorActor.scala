package com.ubirch.avatar.core.actor

import java.io.ByteArrayInputStream

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.HttpExt
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.msgpack.MsgPacker
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.services.util.DeviceCoreUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import org.apache.commons.codec.binary.Hex
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.ValueType

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 18/07/17.
  */
class MessageMsgPackProcessorActor(implicit mongo: MongoUtil, httpClient: HttpExt, materializer: Materializer)
  extends Actor
    with MyJsonProtocol


    with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val validatorActor = context.system.actorOf(Props(new MessageValidatorActor()))
  //private val validatorActor = context.system.actorSelection(ActorNames.MSG_VALIDATOR)

  override def receive: Receive = {

    case binData: Array[Byte] =>
      val s = sender()

      try {
        val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
        unpacker.getNextType match {
          case ValueType.ARRAY =>
            processMsgPack(sender, binData) match {
              case Some(ddrs) =>
                ddrs.foreach(validatorActor forward _)
              case None =>
                val hexVal = Hex.encodeHexString(binData)
                s ! JsonErrorResponse(errorType = "Validation Error", errorMessage = s"Invalid MsgPack Input Data: $hexVal")
            }
          //s ! JsonResponse(message = "simple bulk processing startet")
          case ValueType.INTEGER =>
            processLegacyMsgPack(binData)
          case vt: ValueType =>
            log.error(s"invalid messagePack header type: ${vt.name()}")
        }
      }
      catch {
        case e: Exception =>
          log.error("received invalid data", e)
          sender ! JsonErrorResponse(errorType = "Invalid Data Error", errorMessage = "Invalid Dataformat")
      }
    case _ =>
      log.error("received unknown msgPack message ")
      sender ! JsonErrorResponse(errorType = "Validation Error", errorMessage = "Invalid Input Data")
  }


  private def processMsgPack(s: ActorRef, binData: Array[Byte]): Option[List[DeviceDataRaw]] = {
    val hexVal = Hex.encodeHexString(binData)
    log.info(s"got some msgPack data: $hexVal")

    MsgPacker.unpackTimeseries(binData) match {
      case Some(mpData) =>
        log.debug(s"msgPack data. $mpData")
        val ddrs = mpData.payload.children.map { p =>
          DeviceDataRaw(
            v = MessageVersion.v001,
            a = HashUtil.sha512Base64(mpData.hwDeviceId.toLowerCase),
            s = Some(DeviceCoreUtil.createSimpleSignature(p, mpData.hwDeviceId)),
            mpraw = Some(hexVal),
            p = p,
            ts = mpData.created
          )
        }
        Some(ddrs)
      case None =>
        None
    }
  }

  private def processLegacyMsgPack(binData: Array[Byte]) = {

    val hexVal = Hex.encodeHexString(binData)
    log.info(s"got some legacyMsgPack data: $hexVal")

    val cData = MsgPacker.unpackSingleValue(binData)
    log.debug(s"msgPack data. $cData")
    cData foreach { cd =>
      val hwDeviceId = cd.deviceId.toString.toLowerCase()
      val ddr = DeviceDataRaw(
        v = if (cd.signature.isDefined) MessageVersion.v002 else MessageVersion.v000,
        a = HashUtil.sha512Base64(hwDeviceId.toLowerCase),
        did = Some(cd.deviceId.toString),
        mpraw = Some(hexVal),
        p = cd.payload,
        //k = Some(Base64.getEncoder.encodeToString(Hex.decodeHex("80061e8dff92cde5b87116837d9a1b971316371665f71d8133e0ca7ad8f1826a".toCharArray))),
        s = cd.signature
      )
      validatorActor forward ddr
    }
  }

}
