package com.ubirch.avatar.core.actor

import java.io.ByteArrayInputStream

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.HttpExt
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import akka.util.Timeout
import com.ubirch.avatar.config.{Config, Const}
import com.ubirch.avatar.core.msgpack.{MsgPacker, UbMsgPacker}
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceDataRaws}
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil
import org.apache.commons.codec.binary.Hex
import org.joda.time.{DateTime, DateTimeZone}
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
          case ValueType.ARRAY =>
            processMsgPack(binData)
          case ValueType.INTEGER =>
            processLegacyMsgPack(binData)
          case vt: ValueType => vt
        }) match {
          case ddrs: DeviceDataRaws if ddrs.ddrs.nonEmpty =>
            ddrs.ddrs.foreach(ddr => validatorActor forward ddr)
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
          log.error(e, s"received invalid data")
          sender ! JsonErrorResponse(errorType = "ValidationError", errorMessage = s"invalid dataformat ${e.getMessage}")
      }
    case _ =>
      log.error("received unknown msgPack message ")
      sender ! JsonErrorResponse(errorType = "Validation Error", errorMessage = "invalid input data")
  }


  private def processMsgPack(binData: Array[Byte]): DeviceDataRaws = {

    val hexVal = Hex.encodeHexString(binData)
    log.info(s"got some msgPack data: $hexVal")

    val ddrs: Set[DeviceDataRaw] = MsgPacker.getMsgPackVersion(binData) match {
      case mpv if mpv.version.equals(Const.MSGP_V41) =>
        // process ubirch Protocoll
        UbMsgPacker.processUbirchprot(binData).map { ubm =>
          DeviceDataRaw(
            v = if (ubm.msgType == 83) MessageVersion.v002 else MessageVersion.v000,
            fw = ubm.firmwareVersion.getOrElse("n.a."),
            umv = Some(ubm.mainVersion),
            usv = Some(ubm.subVersion),
            a = ubm.hashedHwDeviceId,
            s = ubm.signature,
            ps = ubm.prevSignature,
            mpraw = Some(ubm.rawMessage),
            mppay = Some(ubm.rawPayload),
            p = ubm.payloads.data,
            config = ubm.payloads.config,
            meta = ubm.payloads.meta,
            ts = DateTime.now(DateTimeZone.UTC)
          )
        }
      case mpv if mpv.version.equals(Const.MSGP_V40) && mpv.firmwareVersion.startsWith("v0.3.1-") =>
        MsgPacker.unpackTimeseries(binData) match {
          case Some(mpData) =>
            log.debug(s"msgPack data. $mpData")
            val refId = UUIDUtil.uuid
            mpData.payloadJson.children.grouped(1000).toList.map { gr =>
              Json4sUtil.any2jvalue(gr) match {
                case Some(p) =>
                  Some(DeviceDataRaw(
                    v = MessageVersion.v000,
                    fw = mpData.firmwareVersion,
                    a = HashUtil.sha512Base64(mpData.hwDeviceId.toLowerCase),
                    s = mpData.signature,
                    mpraw = Some(hexVal),
                    ps = mpData.prevMessageHash,
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
            mpData.payloadJson.children.toList.map { gr =>
              Json4sUtil.any2jvalue(gr) match {
                case Some(p) =>
                  Some(DeviceDataRaw(
                    v = MessageVersion.v000,
                    fw = mpData.firmwareVersion,
                    a = HashUtil.sha512Base64(mpData.hwDeviceId.toLowerCase),
                    s = mpData.signature,
                    mpraw = Some(hexVal),
                    ps = mpData.prevMessageHash,
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
              v = MessageVersion.v000,
              fw = mpData.firmwareVersion,
              a = HashUtil.sha512Base64(mpData.hwDeviceId.toLowerCase),
              s = mpData.signature,
              mpraw = Some(hexVal),
              ps = mpData.prevMessageHash,
              p = mpData.payloadJson,
              ts = mpData.created
            ))
          case None =>
            Set()
        }
      case _ =>
        throw new Exception("unsupported msgpack version")
    }
    DeviceDataRaws(ddrs)
  }

  private def processLegacyMsgPack(binData: Array[Byte]): DeviceDataRaws = {

    val mpRaw = Hex.encodeHexString(binData)
    log.info(s"got some legacyMsgPack data: $mpRaw")

    val cData = MsgPacker.unpackSingleValue(binData)
    log.debug(s"msgPack data. $cData")
    val ddrs = cData map {
      cd =>
        val hwDeviceId = cd.deviceId.toString.toLowerCase()

        DeviceDataRaw(
          v = if (cd.signature.isDefined) MessageVersion.v002 else MessageVersion.v000,
          a = HashUtil.sha512Base64(
            hwDeviceId.trim.toLowerCase),
          did = Some(cd.deviceId.toString),
          mpraw = Some(mpRaw),
          mppay = if (cd.payloadBin.isDefined) Some(Hex.encodeHexString(cd.payloadBin.get)) else None,
          p = cd.payloadJson,
          s = cd.signature
        )
    }
    DeviceDataRaws(ddrs)
  }
}

object MessageMsgPackProcessorActor {
  def props()(implicit mongo: MongoUtil,
              httpClient: HttpExt,
              materializer: Materializer): Props = new RoundRobinPool(Config.akkaNumberOfBackendWorkers)
    .props(Props(new MessageMsgPackProcessorActor()))
}
