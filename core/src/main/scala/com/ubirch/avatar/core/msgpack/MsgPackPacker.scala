package com.ubirch.avatar.core.msgpack

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.core.util.StringConstants._
import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.{ DeviceDataRaw, DeviceStateUpdate }
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.protocol.ProtocolMessage
import com.ubirch.protocol.codec.MsgPackProtocolDecoder
import com.ubirch.server.util.ServerKeys
import com.ubirch.util.crypto.ecc.EccUtil
import com.ubirch.util.crypto.hash.HashUtil
import com.ubirch.util.json.{ Json4sUtil, MyJsonProtocol }
import com.ubirch.util.uuid.UUIDUtil
import org.apache.commons.codec.binary.Hex
import org.joda.time.{ DateTime, DateTimeZone }
import org.json4s.JObject
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.msgpack.core.MessagePack

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.{ Base64, UUID }

object MsgPackPacker extends StrictLogging with MyJsonProtocol {

  final private val SIGPARTLEN: Int = 67
  private val eccUtil = new EccUtil()
  private val config = new MessagePack.PackerConfig().withStr8FormatSupport(false)

  def processUbirchProt(binData: Array[Byte]): DeviceDataRaw = {

    try {
      val pM = MsgPackProtocolDecoder.getDecoder.decode(binData)

      val payloads = parsePayLoads(pM)
      val hwDeviceId = pM.getUUID

      DeviceDataRaw(
        v = MessageVersion.v000,
        fw = payloads.meta.flatMap(m => (m \ "version").extractOpt[String]).getOrElse("n.a."),
        umv = Option(pM.getVersion >> 4),
        usv = Option(pM.getVersion & 0x0f),
        a = DeviceUtil.hashHwDeviceId(hwDeviceId),
        s = Some(Base64.getEncoder.encodeToString(pM.getSignature)),
        ps = Some(Base64.getEncoder.encodeToString(pM.getChain)),
        mpraw = Some(Hex.encodeHexString(binData)),
        mppay = Some(Hex.encodeHexString(binData.take(binData.length - SIGPARTLEN))),
        mppayhash = Some(HashUtil.sha512Hex(binData.take(binData.length - SIGPARTLEN))),
        p = payloads.data,
        config = payloads.config,
        meta = payloads.meta,
        ts = DateTime.now(DateTimeZone.UTC)
      )
    } catch {
      case ex: Throwable =>
        logger.error(PARSING_MSG_PACK_FAILED, ex)
        throw InvalidDataException(PARSING_MSG_PACK_FAILED)
    }
  }

  private def parsePayLoads(decodedPM: ProtocolMessage): Payloads = {
    try {
      val p = decodedPM.getPayload
      val v = new String(p.get(0).binaryValue(), StandardCharsets.UTF_8)
      val w = p.get(1).asLong()
      val s = p.get(2).asLong()
      val m = Some(("version" -> v) ~ ("wakeups" -> w) ~ ("status" -> s))

      Payloads(
        data = Some(parseT84Measurements(fromJsonNode(p.get(3)))),
        meta = m,
        config = Some(parseT84Config(fromJsonNode(p.get(4))))
      )
    } catch {
      case ex: Throwable =>
        logger.error(PARSING_TRACKLE_PAYLOAD_FAILED, ex)
        throw InvalidDataException(PARSING_TRACKLE_PAYLOAD_FAILED)
    }
  }

  private def parseT84Measurements(mVal: JValue): JValue = {
    try {
      val msm = mVal
        .extract[Map[Long, Int]]
        .toSeq
        .sortWith(_._1 <= _._1)
        .map {
          case (ts1, temp) =>
            val ts = new DateTime(ts1 * 1000, DateTimeZone.UTC)
            ("t" -> temp) ~
              ("ts" -> ts.toString)
        }
      Json4sUtil.any2jvalue(msm).get
    } catch {
      case ex: Throwable => throw InvalidDataException(PARSING_TEMPS_FAILED + mVal, ex)
    }
  }

  private def parseT84Config(mVal: JValue): JValue = {
    try {
      JObject(
        mVal
          .extract[Map[String, Long]]
          .toList
          .map {
            case (key: String, value: Long) => key -> JLong(value)
          }
      )
    } catch {
      case ex: Throwable =>
        throw InvalidDataException(PARSING_DEVICE_CONFIG_FAILED + mVal, ex)
    }
  }

  /**
    * Creates the trackle response with
    * version: 1.3 (19)
    * hint: 85
    * payload: deviceConfig
    * chained: prevSignature
    * signature: signed with avatarService key
    * for more info check: https://github.com/ubirch/ubirch-protocol/blob/57920e5cf93c977bc63d649aa86aab8588127e64/README_PAYLOAD.md
    */
  def packUbProt(dsu: DeviceStateUpdate, uuid: UUID = UUIDUtil.uuid): Either[Unit, Array[Byte]] = {
    try {
      val out = new ByteArrayOutputStream(255)
      val packer = config.newPacker(out)
      packer.packArrayHeader(6)

      //pack version 1.3
      packer.packInt((1 << 4) + 3)

      //pack uuid
      val binUuid = UUIDUtil.toByteArray(uuid)
      packer.packRawStringHeader(binUuid.length)
      packer.writePayload(binUuid)

      //pack prev signature
      val prevSign = dsu.ds.getOrElse(throw InvalidDataException(NO_PREV_SIGNATURE_FOUND))
      val binPrevSignature = Base64.getDecoder.decode(prevSign)
      packer.packRawStringHeader(binPrevSignature.length)
      packer.writePayload(binPrevSignature)

      //pack hint
      packer.packInt(85)

      //pack config = payload
      val (configMap, eolBoolean) = separateIntAndBooleanValues(dsu)
      packer.packMapHeader(configMap.keySet.size + 1)
      //pack boolean
      packer.packString("EOL")
      packer.packBoolean(eolBoolean)
      //pack integers
      configMap.foreach {
        case (key, value) =>
          packer.packString(key)
          packer.packInt(value)
      }

      //create signature
      packer.flush()
      val payloadBin = out.toByteArray
      val hashedPayload = HashUtil.sha512(payloadBin)
      val signatureB64 = eccUtil.signPayload(eddsaPrivateKey = ServerKeys.privateKey, payload = hashedPayload)
      val byteSignature = Base64.getDecoder.decode(signatureB64)

      //pack signature
      packer.packRawStringHeader(byteSignature.length)
      packer.writePayload(byteSignature)
      packer.close()
      Right(out.toByteArray)
    } catch {
      case ex: Throwable =>
        logger.error(s"$PACKING_TRACKLE_RESPONSE_FAILED with deviceState $dsu failed: ${ex.getMessage} ", ex)
        Left()
    }
  }

  private def separateIntAndBooleanValues(dsu: DeviceStateUpdate): (Map[String, Int], Boolean) =
    try {
      val deviceConfig = dsu.p.asInstanceOf[JObject]
      val (remainingConfig, eolBoolean) = deviceConfig.findField {
        case JField("EOL", _) => true
        case _                => false
      } match {
        case Some(field) if field._2.extract[Boolean] =>
          (deviceConfig.removeField(_ == field).asInstanceOf[JObject], true)
        case Some(field) => (deviceConfig.removeField(_ == field).asInstanceOf[JObject], false)
        case _           => (deviceConfig, false)
      }
      (remainingConfig.extract[Map[String, Int]], eolBoolean)
    } catch {
      case ex: Throwable =>
        logger.error(PARSING_DEVICE_CONFIG_FAILED, ex)
        throw InvalidDataException(PARSING_DEVICE_CONFIG_FAILED)
    }

  case class InvalidDataException(message: String) extends Exception(message)

  object InvalidDataException {
    def apply(message: String, cause: Throwable): Throwable = {
      val exc = InvalidDataException(message)
      exc.initCause(cause)
    }
  }

  case class Payloads(
    data: JValue,
    meta: Option[JValue] = None,
    config: Option[JValue] = None
  )

}
