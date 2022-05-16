package com.ubirch.avatar.core.msgpack

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.model.rest.device.DeviceStateUpdate
import com.ubirch.avatar.model.rest.ubp.{UbMessage, UbPayloads}
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.protocol.ProtocolMessage
import com.ubirch.protocol.codec.MsgPackProtocolDecoder
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
import org.msgpack.core.MessagePack

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.{Base64, UUID}

object UbMsgPacker
  extends StrictLogging
    with MyJsonProtocol {

  private final val SIGPARTLEN: Int = 67
  private val eccUtil = new EccUtil()
  private val config = new MessagePack.PackerConfig().withStr8FormatSupport(false)

  def processUbirchProt(binData: Array[Byte]): Set[UbMessage] = {

    val pM = MsgPackProtocolDecoder.getDecoder.decode(binData)

    val payloads = parsePayLoads(pM)
    val hwDeviceId = pM.getUUID

    Set(UbMessage(
      version = pM.getVersion,
      mainVersion = pM.getVersion >> 4,
      subVersion = pM.getVersion & 0x0f,
      hwDeviceId = hwDeviceId,
      hashedHwDeviceId = DeviceUtil.hashHwDeviceId(hwDeviceId),
      //Todo: double check extract in case of error
      firmwareVersion = payloads.meta.flatMap(m => (m \ "version").extractOpt[String]),
      prevSignature = Some(Base64.getEncoder.encodeToString(pM.getChain)),
      msgType = pM.getHint,
      payloads = payloads,
      signature = Some(Base64.getEncoder.encodeToString(pM.getSignature)),
      rawPayload = Hex.encodeHexString(binData.take(binData.length - SIGPARTLEN)),
      rawMessage = Hex.encodeHexString(binData),
      payloadHash = HashUtil.sha512Hex(binData.take(binData.length - SIGPARTLEN))
    ))
  }


  private def parsePayLoads(decodedPM: ProtocolMessage): UbPayloads = {
    val p = decodedPM.getPayload
    val v = new String(p.get(0).binaryValue(), StandardCharsets.UTF_8)
    val w = p.get(1).asLong()
    val s = p.get(2).asLong()
    val m = Some(("version" -> v) ~ ("wakeups" -> w) ~ ("status" -> s))

    UbPayloads(
      data = Some(parseT84Measurements(fromJsonNode(p.get(3)))),
      meta = m,
      config = Some(parseT84Config(fromJsonNode(p.get(4))))
    )
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
      case ex: Throwable =>
        throw InvalidDataException(s"something went wrong parsing temperature measurements $mVal", ex)
    }
  }

  private def parseT84Config(mVal: JValue): JValue =
    JObject(
      mVal
        .extract[Map[String, Long]]
        .toList
        .map {
          //Todo: handle non-matching case??
          case (key: String, value: Long) => key -> JLong(value)
        }
    )

  def packUbProt(dsu: DeviceStateUpdate, uuid: UUID = UUIDUtil.uuid): Array[Byte] = {
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
      val binPrevSignature = Base64.getDecoder.decode(dsu.ds.get)
      packer.packRawStringHeader(binPrevSignature.length)
      packer.writePayload(binPrevSignature)

      //pack hint
      packer.packInt(85)

      //pack config = payload
      val deviceConfig = dsu.p.asInstanceOf[JObject]
      val (remainingConfig, eolBoolean) = deviceConfig.findField {
        case JField("EOL", _) => true
        case _ => false
      } match {
        case Some(field) if field._2.extract[Boolean] => (deviceConfig.removeField(_ == field).asInstanceOf[JObject], true)
        case Some(field) => (deviceConfig.removeField(_ == field).asInstanceOf[JObject], false)
        case _ => (deviceConfig, false)
      }
      val pl = remainingConfig.extract[Map[String, Int]]
      val plKeys = pl.keySet
      packer.packMapHeader(plKeys.size + 1)
      packer.packString("EOL")
      packer.packBoolean(eolBoolean)
      plKeys.foreach { k: String =>
        if (pl.contains(k)) {
          packer.packString(k)
          packer.packInt(pl(k))
        }
      }

      //create signature
      packer.flush()
      val payloadBin = out.toByteArray
      val hashedPayload = HashUtil.sha512(payloadBin)
      val signatureB64 = eccUtil.signPayload(eddsaPrivateKey = ServerKeys.privateKey,
        payload = hashedPayload,
        encoding = "b64")
      val byteSignature = Base64.getDecoder.decode(signatureB64)

      //pack signature
      packer.packRawStringHeader(byteSignature.length)
      packer.writePayload(byteSignature)
      packer.close()
      out.toByteArray
    } catch {
      case ex: Throwable =>
        logger.error(s"packing ubirch protocol (extracting deviceStateUpdate) for response failed: ${ex.getMessage} ", ex)
        throw ex
    }
  }

  case class InvalidDataException(message: String, cause: Throwable) extends Exception(message, cause)

}
