package com.ubirch.avatar.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.HttpExt
import akka.routing.RoundRobinPool
import akka.stream.Materializer
import akka.util.Timeout
import com.ubirch.avatar.config.{Config, Const}
import com.ubirch.avatar.core.msgpack.{MsgPacker, UbMsgPacker}
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.{DeviceDataRaw, DeviceDataRaws}
import com.ubirch.avatar.model.rest.ubp.UbMessage
import com.ubirch.avatar.util.actor.ActorNames
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import org.apache.commons.codec.binary.Hex
import org.joda.time.{DateTime, DateTimeZone}
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.ValueType

import java.io.ByteArrayInputStream
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
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val validatorActor = context.actorSelection(ActorNames.MSG_VALIDATOR_PATH)

  override def receive: Receive = {

    case binData: Array[Byte] =>
      val s = sender()
      try {

        val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))

        (unpacker.getNextType match {
          case ValueType.ARRAY => processMsgPack(binData)
          case vt: ValueType => vt
        }) match {
          case ddrs: DeviceDataRaws if ddrs.ddrs.nonEmpty =>
            log.info(s"validating msgpack data (ddrs): ${ddrs.ddrs.size}")
            ddrs.ddrs.foreach(ddr => validatorActor forward ddr)
          case vt: ValueType =>
            val em = s"invalid messagePack header type: ${vt.name()}"
            log.error(em)
            s ! JsonErrorResponse(errorType = "validation error", errorMessage = em)
          case _ =>
            s ! JsonErrorResponse(errorType = "validation error", errorMessage = "invalid bin data")
        }
      } catch {
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
    log.debug(s"got some msgPack data: $hexVal")

    val ddrs: Set[DeviceDataRaw] = MsgPacker.getMsgPackVersion(binData) match {
      case mpv if mpv.version.equals(Const.MSGP_V41) =>
        UbMsgPacker.processUbirchprot(binData).map { ubm: UbMessage =>
          log.debug(s"msgPack data version=${Const.MSGP_V41} ${ubm.hwDeviceId}")
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
            mppayhash = Some(ubm.payloadHash),
            p = ubm.payloads.data,
            config = ubm.payloads.config,
            meta = ubm.payloads.meta,
            ts = DateTime.now(DateTimeZone.UTC)
          )
        }
      case _ =>
        throw new Exception("unsupported msgpack version")
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
