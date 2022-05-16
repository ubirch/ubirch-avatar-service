package com.ubirch.avatar.core.msgpack

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.model.rest.device.DeviceStateUpdate
import com.ubirch.avatar.model.rest.ubp.{UbMessage, UbPayloads}
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.server.util.ServerKeys
import com.ubirch.util.crypto.ecc.EccUtil
import com.ubirch.util.crypto.hash.HashUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.uuid.UUIDUtil
import org.apache.commons.codec.binary.Hex
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JObject
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.{MapValue, Value, ValueType}

import java.io.ByteArrayInputStream
import java.util.Base64
import scala.collection.JavaConversions._

object UbMsgPacker
  extends StrictLogging
    with MyJsonProtocol {

  private final val SIGPARTLEN: Int = 67
  private val eccUtil = new EccUtil()

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
      case 84 =>
        processT84Payload(payload)
      case n: Int =>
        throw new Exception(s"unsupported msg pack payload type $n")
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

    try {
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
      val config = dsu.p.asInstanceOf[JObject]

      val (remainingConfig, eolBoolean) = config.findField {
        case JField("EOL", _) => true
        case _ => false
      } match {
        case Some(field) if field._2.extract[Boolean] => (config.removeField(_ == field).asInstanceOf[JObject], true)
        case Some(field) => (config.removeField(_ == field).asInstanceOf[JObject], false)
        case _ => (config, false)
      }

      val pl = remainingConfig.extract[Map[String, Int]]
      val plKeys = pl.keySet
      packer.writeMapBegin(plKeys.size + 1)
      packer.write("EOL")
      packer.write(eolBoolean)
      plKeys.foreach { k: String =>
        if (pl.contains(k)) {
          packer.write(k)
          packer.write(pl(k))
        }
      }
      packer.writeMapEnd()

      val payloadBin = packer.toByteArray


      val signatureB64 = eccUtil.signPayloadSha512(eddsaPrivateKey = ServerKeys.privateKey, payload = payloadBin)
      packer.write(Base64.getDecoder.decode(signatureB64))
      packer.writeArrayEnd(true)
      packer.toByteArray
    } catch {
      case ex: Throwable =>
        logger.error(s"packing ubirch protocol (extracting deviceStateUpdate) for response failed: ${ex.getMessage} ", ex)
        throw ex
    }
  }

}
