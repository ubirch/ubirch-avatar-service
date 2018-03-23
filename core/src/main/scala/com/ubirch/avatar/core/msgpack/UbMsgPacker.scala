package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.model.rest.ubp.{UbMessage, UbPayloads}
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.uuid.UUIDUtil
import org.apache.commons.codec.binary.Hex
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{compact, render}
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.{MapValue, Value, ValueType}

import scala.collection.JavaConversions._


object UbMsgPacker
  extends StrictLogging
    with MyJsonProtocol {

  private final val SIGPARTLEN: Int = 67

  def processUbirchprot(binData: Array[Byte]): Set[UbMessage] = {
    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
    val itr = unpacker.iterator().toSet
    itr.map { v =>
      processUbirchprot(binData, v)
    }.filter(_.isDefined).map(_.get)
  }

  private def processUbirchprot(binData: Array[Byte], v: Value): Option[UbMessage] = {
    val va = v.asArrayValue()
    val version = va.get(0).asIntegerValue().getInt
    val mainVersion = version >> 4
    val subVersion = version & 15

    val rawUuid = va.get(1).asRawValue().getByteArray
    val uuid: java.util.UUID = UUIDUtil.fromByteArray(rawUuid)
    logger.debug(s"uuid $uuid")

    logger.debug(s"main version $mainVersion")

    subVersion match {
      case 1 =>
        logger.debug("format v1")

        val messageType = va.get(2).asIntegerValue().getInt
        logger.debug(s"messageType $messageType")

        val payload = va.get(3)

        logger.debug(s"payload type: ${payload.getType}")
        val payloads = processPayload(messageType, payload)

        Some(UbMessage(
          version = version,
          mainVersion = mainVersion,
          subVersion = subVersion,
          hwDeviceId = uuid,
          hashedHwDeviceId = DeviceUtil.hashHwDeviceId(uuid),
          firmwareVersion = (payloads.data \ "version").extractOpt[String],
          prevSignature = None,
          msgType = messageType,
          payloads = payloads,
          signature = None,
          rawPayload = Hex.encodeHexString(payload.asRawValue().getByteArray),
          rawMessage = Hex.encodeHexString(binData)
        ))

      case 2 =>
        logger.debug("format v2")

        val messageType = va.get(2).asIntegerValue().getInt
        logger.debug(s"messageType $messageType")

        val payload = va.get(3)
        logger.debug(s"payload type: ${payload.getType}")

        val payloadJson = processPayload(messageType, payload)

        val rawSignature = va.get(4).asRawValue().getByteArray
        val signature = Hex.encodeHexString(rawSignature)
        logger.debug(s"signture $signature")
        Some(UbMessage(
          version = version,
          mainVersion = mainVersion,
          subVersion = subVersion,
          hwDeviceId = uuid,
          hashedHwDeviceId = DeviceUtil.hashHwDeviceId(uuid),
          firmwareVersion = (payloadJson.data \ "version").extractOpt[String],
          prevSignature = None,
          msgType = messageType,
          payloads = payloadJson,
          signature = Some(signature),
          rawPayload = Hex.encodeHexString(binData.take(binData.length - SIGPARTLEN)),
          rawMessage = Hex.encodeHexString(binData)
        ))

      case 3 =>
        logger.debug("format v3")

        val rawPrevHash = va.get(2).asRawValue().getByteArray
        val prevHash = Hex.encodeHexString(rawPrevHash)
        logger.debug(s"prevHash $prevHash")

        val messageType = va.get(3).asIntegerValue().getInt
        logger.debug(s"messageType $messageType")

        val payload = va.get(4)
        logger.debug(s"payload type: ${payload.getType}")

        val payloads = processPayload(messageType, payload)

        val rawSignature = va.get(5).asRawValue().getByteArray
        val signature = Hex.encodeHexString(rawSignature)
        logger.debug(s"signture $signature")

        val fw = if (payloads.meta.isDefined)
          (payloads.meta.get \ "version").extractOpt[String]
        else
          None

        Some(UbMessage(
          version = version,
          mainVersion = mainVersion,
          subVersion = subVersion,
          hwDeviceId = uuid,
          hashedHwDeviceId = DeviceUtil.hashHwDeviceId(uuid),
          firmwareVersion = fw,
          prevSignature = Some(prevHash),
          msgType = messageType,
          payloads = payloads,
          signature = Some(signature),
          rawPayload = Hex.encodeHexString(binData.take(binData.length - SIGPARTLEN)),
          rawMessage = Hex.encodeHexString(binData)
        ))

      case _ =>
        throw new Exception("unknown ubirch protocol message payload")
    }
  }


  private def processPayload(messageType: Int, payload: Value): UbPayloads = {
    messageType match {
      case 83 =>
        throw new Exception("not implemented ubirch protocol T85")
      case 84 =>
        processT84Payload(payload)
      case 85 =>
        throw new Exception("not implemented ubirch protocol T85")
      case n: Int =>
        throw new Exception(s"unsupported msg type $n")
    }
  }

  private def processT84Payload(payload: Value): UbPayloads = {
    if (payload.asArrayValue().size() == 5) {
      logger.debug("playload size OK")
      val payArr = payload.asArrayValue()
      val version = payArr.get(0).asRawValue().getString
      val wakeups = payArr.get(1).asIntegerValue().getLong
      val status = payArr.get(2).asIntegerValue().getLong

      val meta = ("version" -> version) ~
        ("wakeups" -> wakeups) ~
        ("status" -> status)

      val mMap = payArr.get(3).asMapValue()
      val cMap = payArr.get(4).asMapValue()
      logger.debug(s"v: $version / w: $wakeups / s: $status")
      UbPayloads(
        data = parseMeasurementMap(mMap),
        meta = Some(meta),
        config = Some(parseConfigMap(cMap))
      )
    }
    else
      throw new Exception("playload size not OK")
  }

  private def parseMeasurementMap(mVal: MapValue): JValue = {
    val res = mVal.keySet.toArray.map { key =>
      val curVal = mVal.get(key)
      val tsMillis = key.toString.toLong * 1000
      val ts = new DateTime(tsMillis, DateTimeZone.UTC)
      curVal.getType match {
        case ValueType.INTEGER =>
          val curValVal = curVal.asIntegerValue().getLong
          logger.debug(s"k: $ts ($key) -> v: $curValVal")
          Some((ts.toString -> JLong(curValVal)))
        case ValueType.RAW =>
          val curValVal = curVal.asRawValue().getString
          logger.debug(s"k: $ts ($key) -> v: $curValVal")
          Some((ts.toString -> JString(curValVal)))
        case ValueType.BOOLEAN =>
          val curValVal = curVal.asBooleanValue().getBoolean
          logger.debug(s"k: $ts ($key) -> v: $curValVal")
          Some((ts.toString -> JBool(curValVal)))
        case ValueType.FLOAT =>
          val curValVal = curVal.asFloatValue().getDouble
          logger.debug(s"k: $ts ($key) -> v: $curValVal")
          Some((ts.toString -> JDouble(curValVal)))
        case _ =>
          logger.debug("unsupported measurement type")
          None
      }
    }.filter(_.isDefined).map(_.get).toList
    val json = JObject(res)
    logger.debug(compact(render(json)))
    json
  }

  private def parseConfigMap(mVal: MapValue): JValue = {
    val res = mVal.keySet.toArray.map { key =>
      val keyStr = String.valueOf(key).replace("\"", "")
      val curVal = mVal.get(key)
      curVal.getType match {
        case ValueType.INTEGER =>
          val curValVal = curVal.asIntegerValue().getLong
          logger.debug(s"k: ${keyStr} ($key) -> v: $curValVal")
          Some((keyStr -> JLong(curValVal)))
        case ValueType.RAW =>
          val curValVal = curVal.asRawValue().getString
          logger.debug(s"k: $keyStr ($key) -> v: $curValVal")
          Some((keyStr -> JString(curValVal)))
        case _ =>
          logger.debug("unsupported config type")
          None
      }
    }.filter(_.isDefined).map(_.get).toList
    val json = JObject(res)
    logger.debug(compact(render(json)))
    json
  }

}
