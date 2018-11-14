package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream
import java.util.Base64

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.model.rest.device.DeviceStateUpdate
import com.ubirch.avatar.model.rest.ubp.{UbMessage, UbPayloads}
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.crypto.ecc.EccUtil
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.server.util.ServerKeys
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.uuid.UUIDUtil
import org.apache.commons.codec.binary.Hex
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.{ArrayValue, MapValue, Value, ValueType}

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
          rawMessage = Hex.encodeHexString(binData),
          payloadHash = HashUtil.sha512Hex(payload.asRawValue().getByteArray)
        ))

      case 2 =>
        logger.debug("format v2")

        val messageType = va.get(2).asIntegerValue().getInt
        logger.debug(s"messageType $messageType")

        val payload = va.get(3)
        logger.debug(s"payload type: ${payload.getType}")

        val payloadJson = processPayload(messageType, payload)

        val rawSignature = va.get(4).asRawValue().getByteArray
        //val signature = Hex.encodeHexString(rawSignature)
        val signature = Base64.getEncoder.encodeToString(rawSignature)
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
          rawMessage = Hex.encodeHexString(binData),
          payloadHash = HashUtil.sha512Hex(binData.take(binData.length - SIGPARTLEN))
        ))

      case 3 =>
        logger.debug("format v3")

        val rawPrevHash = va.get(2).asRawValue().getByteArray
        val prevSignature = Base64.getEncoder.encodeToString(rawPrevHash)
        logger.debug(s"prevHash $prevSignature")

        val messageType = va.get(3).asIntegerValue().getInt
        logger.debug(s"messageType $messageType")

        val payload = va.get(4)
        logger.debug(s"payload type: ${payload.getType}")

        val payloads = processPayload(messageType, payload)

        val rawSignature = va.get(5).asRawValue().getByteArray
        //val signature = Hex.encodeHexString(rawSignature)
        val signature = Base64.getEncoder.encodeToString(rawSignature)
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
          prevSignature = Some(prevSignature),
          msgType = messageType,
          payloads = payloads,
          signature = Some(signature),
          rawPayload = Hex.encodeHexString(binData.take(binData.length - SIGPARTLEN)),
          rawMessage = Hex.encodeHexString(binData),
          payloadHash = HashUtil.sha512Hex(binData.take(binData.length - SIGPARTLEN))
        ))

      case _ =>
        throw new Exception("unknown ubirch protocol message payload")
    }
  }


  private def processPayload(messageType: Int, payload: Value): UbPayloads = {
    messageType match {
      case 0x00 =>
        processGenericMessage(payload);
      case 50 =>
        processT50Payload(payload.asArrayValue())
      case 83 =>
        processT83Payload(payload.asMapValue())
      case 84 =>
        processT84Payload(payload)
      case 85 =>
        throw new Exception("not implemented ubirch protocol T85")
      case n: Int =>
        throw new Exception(s"unsupported msg type $n")
    }
  }

  private def processGenericMessage(payload: Value): UbPayloads = {
    payload.getType match {
      case ValueType.RAW => UbPayloads("hash" -> Base64.getEncoder.encodeToString(payload.asRawValue().getByteArray))
      case t: ValueType =>
        throw new Exception(s"unsupported message content: ${t.toString}")
    }
  }

  private def processT50Payload(payload: Value): UbPayloads = {
    if (payload.isArrayValue) {
      val payLoads = payload.asArrayValue()
      if (payLoads.size() > 0) {
        val data = if (payLoads.get(0).isArrayValue)
          parseArray(payload.asArrayValue())
        else
          parseSimpleArray(payload.asArrayValue())

        UbPayloads(
          data,
          meta = None,
          config = None
        )
      }

      else
        throw new Exception("payload not correct, expected map")
    } else {
      throw new Exception("payload not correct, expected map")
    }
  }

  private def processT83Payload(payload: Value): UbPayloads = {
    if (payload.isMapValue) {
      UbPayloads(
        data = parseMap(payload.asMapValue()),
        meta = None,
        config = None
      )
    } else {
      throw new Exception("payload not correct, expected map")
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
        data = parseT84Measurements(mMap),
        meta = Some(meta),
        config = Some(parseConfigMap(cMap))
      )
    }
    else
      throw new Exception("payload size not OK")
  }

  private def parseT84Measurements(mVal: MapValue): JValue = {
    Json4sUtil.any2jvalue(mVal.keySet.toArray.map { key =>
      val tsMillis = key.toString.toLong * 1000
      val ts = new DateTime(tsMillis, DateTimeZone.UTC)
      val t = mVal.get(key).asIntegerValue().getInt
      ("t" -> t) ~
        ("ts" -> ts.toString)
    }).get
  }

  //@TODO have to be checked!!!
  private def parseMeasurementMap(mVal: MapValue): JValue = {
    val res = mVal.keySet.toArray.map { key =>
      val curVal = mVal.get(key)
      val tsMillis = key.toString.toLong * 1000
      val ts = new DateTime(tsMillis, DateTimeZone.UTC)
      curVal.getType match {
        case ValueType.INTEGER =>
          val curValVal = curVal.asIntegerValue().getLong
          logger.debug(s"k: $ts ($key) -> v: $curValVal")
          Some(ts.toString -> JLong(curValVal))
        case ValueType.RAW =>
          val curValVal = curVal.asRawValue().getString
          logger.debug(s"k: $ts ($key) -> v: $curValVal")
          Some(ts.toString -> JString(curValVal))
        case ValueType.BOOLEAN =>
          val curValVal = curVal.asBooleanValue().getBoolean
          logger.debug(s"k: $ts ($key) -> v: $curValVal")
          Some(ts.toString -> JBool(curValVal))
        case ValueType.FLOAT =>
          val curValVal = curVal.asFloatValue().getDouble
          logger.debug(s"k: $ts ($key) -> v: $curValVal")
          Some(ts.toString -> JDouble(curValVal))
        case _ =>
          logger.debug("unsupported measurement type")
          None
      }
    }.filter(_.isDefined).map(_.get).toList
    val json = JObject(res)
    logger.debug(compact(render(json)))
    json
  }

  private def parseArray(aVal: ArrayValue): JValue = {
    val allPayloads = aVal.getElementArray.toList.map(av => parseSimpleArray(av.asArrayValue()))
    Json4sUtil.any2jvalue(allPayloads)
  }

  private def parseSimpleArray(aVal: ArrayValue): JValue = {
    val keys: Seq[String] = Seq("ts") ++ ('a' to 'z').map(_.toString).map(k => s"value${k.toUpperCase}").take(aVal.getElementArray.length)

    val zipped = aVal.getElementArray.zip(keys).foldLeft(Map[String, Value]()) { (a, b) =>
      a + (b._2 -> b._1)
    }

    val dataMap: Map[String, Option[Any]] = zipped.map { tpl =>
      val key = tpl._1
      val av = tpl._2
      val value: Option[Any] = av.getType match {
        case ValueType.BOOLEAN =>
          Some(av.asBooleanValue().getBoolean)
        case ValueType.INTEGER =>
          if ("ts".equals(key.trim.toLowerCase)) {
            val tsInt: Long = if (av.isIntegerValue)
              av.asIntegerValue().getLong / 1000L
            else
              av.asFloatValue().asFloatValue().getDouble.toLong / 1000L

            //            val ts = new DateTime(tsInt, DateTimeZone.UTC)
            //            Some(ts.toDateTimeISO.toString)
            Some(tsInt)
          }
          else
            Some(BigDecimal(av.asIntegerValue().getBigInteger))
        case ValueType.FLOAT =>
          Some(av.asFloatValue().asFloatValue().getDouble)
        case _ => None
      }
      key -> value
    }
    val cleanData = dataMap.filter(m => m._2.isDefined).map(m => m._1 -> m._2.get)
    Json4sUtil.any2jvalue(cleanData).get
  }


  private def parseMap(mVal: MapValue): JValue = {
    val res = mVal.keySet.toArray.map { key =>
      val keyStr = String.valueOf(key).replace("\"", "")
      val curVal = mVal.get(key)
      curVal.getType match {
        case ValueType.INTEGER =>
          val curValVal = curVal.asIntegerValue().getLong
          logger.debug(s"k: $keyStr ($key) -> v: $curValVal")
          Some(keyStr -> JLong(curValVal))
        case ValueType.RAW =>
          val curValVal = curVal.asRawValue().getString
          logger.debug(s"k: $keyStr ($key) -> v: $curValVal")
          Some(keyStr -> JString(curValVal))
        case ValueType.BOOLEAN =>
          val curValVal = curVal.asBooleanValue().getBoolean
          logger.debug(s"k: $keyStr ($key) -> v: $curValVal")
          Some(keyStr -> JBool(curValVal))
        case ValueType.FLOAT =>
          val curValVal = curVal.asFloatValue().getDouble
          logger.debug(s"k: $keyStr ($key) -> v: $curValVal")
          Some(keyStr -> JDouble(curValVal))
        case _ =>
          logger.debug("unsupported type")
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
          logger.debug(s"k: $keyStr ($key) -> v: $curValVal")
          Some(keyStr -> JLong(curValVal))
        case ValueType.RAW =>
          val curValVal = curVal.asRawValue().getString
          logger.debug(s"k: $keyStr ($key) -> v: $curValVal")
          Some(keyStr -> JString(curValVal))
        case _ =>
          logger.debug("unsupported config type")
          None
      }
    }.filter(_.isDefined).map(_.get).toList
    val json = JObject(res)
    logger.debug(compact(render(json)))
    json
  }

  def packUbProt(dsu: DeviceStateUpdate): Array[Byte] = {

    val packer = ScalaMessagePack.messagePack.createBufferPacker()

    val subversion = if (dsu.ds.isDefined) 3 else 2

    val uuid = UUIDUtil.uuid
    val binUuid = UUIDUtil.toByteArray(uuid)

    val arraySize = if (dsu.ds.isDefined)
      6
    else
      5
    packer.writeArrayBegin(arraySize)
    packer.write((1 << 4) + subversion)
    packer.write(binUuid)
    if (dsu.ds.isDefined) {
      try {
        //        val binSig = Hex.decodeHex(dsu.ds.get)
        val binSig = Base64.getDecoder.decode(dsu.ds.get)
        packer.write(binSig)
      }
      catch {
        case e: Exception =>
          logger.error(s"invalid signature: ${dsu.ds.get}")
      }
    }
    packer.write(85)
    val pl = dsu.p.extract[Map[String, Int]]
    val plKeys = pl.keySet
    packer.writeMapBegin(plKeys.size)
    plKeys.foreach { k =>
      if (pl.get(k).isDefined) {
        packer.write(k)
        packer.write(pl(k))
      }
    }
    packer.writeMapEnd()

    val payloadBin = packer.toByteArray

    val signatureB64 = EccUtil.signPayloadSha512(eddsaPrivateKey = ServerKeys.privateKey, payload = payloadBin)
    packer.write(Base64.getDecoder.decode(signatureB64))
    packer.writeArrayEnd(true)
    packer.toByteArray
  }

}
