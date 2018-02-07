package com.ubirch.avatar.core.actor

import java.io.ByteArrayInputStream

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.util.Timeout
import com.ubirch.avatar.config.{Config, Const}
import com.ubirch.avatar.core.msgpack.MsgPacker
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil
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

  private val validatorActor = context.actorSelection(ActorNames.MSG_VALIDATOR_PATH)

  override def receive: Receive = {

    case binData: Array[Byte] =>
      val s = sender()
      try {
        val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
        (unpacker.getNextType match {
          case ValueType.ARRAY => processMsgPack(binData)
          case ValueType.INTEGER => processLegacyMsgPack(binData)
          case vt: ValueType => vt
        }) match {
          case drds: Set[DeviceDataRaw] if drds.nonEmpty =>
            drds foreach (ddrs => validatorActor forward ddrs)
          case vt: ValueType =>
            val em = s"invalid messagePack header type: ${vt.name()}"
            log.error(em)
            s ! JsonErrorResponse(errorType = "validation error", errorMessage = em)
          case _ =>
            s ! JsonErrorResponse(errorType = "validation error", errorMessage = "invalid bin data")
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


  private def processMsgPack(binData: Array[Byte]): Set[DeviceDataRaw] = {
    val hexVal = Hex.encodeHexString(binData)
    log.info(s"got some msgPack data: $hexVal")

    MsgPacker.getMsgPackVersion(binData) match {
      case mpv if mpv.version.equals(Const.MSGP_V41) =>
        throw new Exception("unsupported msgpack version")
      case mpv if mpv.version.equals(Const.MSGP_V40) && mpv.firmwareVersion.startsWith("v0.3.1-") =>
        MsgPacker.unpackTimeseries(binData) match {
          case Some(mpData) =>
            log.debug(s"msgPack data. $mpData")
            val refId = UUIDUtil.uuid
            mpData.payload.children.grouped(2000).toList.map { gr =>
              Json4sUtil.any2jvalue(gr) match {
                case Some(p) =>
                  Some(DeviceDataRaw(
                    v = MessageVersion.v000,
                    fw = mpData.firmwareVersion,
                    a = HashUtil.sha512Base64(mpData.hwDeviceId.toLowerCase),
                    s = mpData.signature,
                    mpraw = Some(hexVal),
                    //                    mpraw = None,
                    chainedHash = mpData.prevMessageHash,
                    p = p,
                    ts = mpData.created,
                    refId = Some(refId)
                  ))
                case None =>
                  None
              }
            }.filter(_.isDefined).map(_.get).toSet
          case None =>
            Set()
        }
      case mpv if mpv.version.equals(Const.MSGP_V401) && mpv.firmwareVersion.startsWith("v1.0") =>
        MsgPacker.unpackTimeseries(binData) match {
          case Some(mpData) =>
            log.debug(s"msgPack data. $mpData")
            mpData.payload.children.toList.map { gr =>
              Json4sUtil.any2jvalue(gr) match {
                case Some(p) =>
                  Some(DeviceDataRaw(
                    v = MessageVersion.v000,
                    fw = mpData.firmwareVersion,
                    a = HashUtil.sha512Base64(mpData.hwDeviceId.toLowerCase),
                    s = mpData.signature,
                    mpraw = Some(hexVal),
                    chainedHash = mpData.prevMessageHash,
                    p = p,
                    ts = mpData.created
                  ))
                case None =>
                  None
              }
            }.filter(_.isDefined).map(_.get).toSet
          case None =>
            Set()
        }
      case mpv if mpv.version.equals(Const.MSGP_V40) =>
        MsgPacker.unpackTimeseries(binData) match {
          case Some(mpData) =>
            log.debug(s"msgPack data. $mpData")
            Set(DeviceDataRaw(
              v = MessageVersion.v002,
              fw = mpData.firmwareVersion,
              a = HashUtil.sha512Base64(mpData.hwDeviceId.toLowerCase),
              s = mpData.signature,
              mpraw = Some(hexVal),
              chainedHash = mpData.prevMessageHash,
              p = mpData.payload.children.head,
              ts = mpData.created
            ))
          case None =>
            Set()
        }
      case _ =>
        throw new Exception("unsupported msgpack version")
    }
  }

  private def processLegacyMsgPack(binData: Array[Byte]): Set[DeviceDataRaw] = {

    val hexVal = Hex.encodeHexString(binData)
    log.info(s"got some legacyMsgPack data: $hexVal")

    val cData = MsgPacker.unpackSingleValue(binData)
    log.debug(s"msgPack data. $cData")
    cData map {
      cd =>
        val hwDeviceId = cd.deviceId.toString.toLowerCase()
        DeviceDataRaw(
          v = if (cd.signature.isDefined) MessageVersion.v002 else MessageVersion.v000,
          a = HashUtil.sha512Base64(hwDeviceId.toLowerCase),
          did = Some(cd.deviceId.toString),
          mpraw = Some(hexVal),
          p = cd.payload,
          //k = Some(Base64.getEncoder.encodeToString(Hex.decodeHex("80061e8dff92cde5b87116837d9a1b971316371665f71d8133e0ca7ad8f1826a".toCharArray))),
          s = cd.signature
        )
    }
  }
}
