package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream
import java.lang.{Long => JavaLong}
import java.util.Base64

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.model.rest.device.{MsgPackMessage, MsgPackMessageV2}
import com.ubirch.util.json.Json4sUtil
import org.apache.commons.codec.binary
import org.apache.commons.codec.binary.Hex
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST._
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.{Value, ValueType}
import org.msgpack.unpacker.Unpacker

import scala.collection.mutable
import scala.language.postfixOps

case class MsgPackVersion(version: String, firmwareVersion: String)

object MsgPacker extends StrictLogging {

  def getMsgPackVersion(binData: Array[Byte]): MsgPackVersion = {
    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
    val itr = unpacker.iterator()

    unpacker.getNextType match {
      case ValueType.ARRAY if itr.hasNext =>
        val va = itr.next().asArrayValue()
        va.get(0).getType match {
          case ValueType.INTEGER =>
            val version = va.get(0).asIntegerValue().getInt
            val mainVersion = version >> 4
            val subVersion = version & 15
            MsgPackVersion(
              version = Const.MSGP_V41,
              firmwareVersion = "unknown"
            )
          case _ =>
            val firmwareVersion = va.get(1).asRawValue().getString
            va.get(0).asRawValue().getString match {
              case Const.MSGP_V40 =>
                MsgPackVersion(
                  version = Const.MSGP_V40,
                  firmwareVersion = firmwareVersion
                )
              case Const.MSGP_V401 =>
                MsgPackVersion(
                  version = Const.MSGP_V401,
                  firmwareVersion = firmwareVersion
                )
              case _ =>
                MsgPackVersion(
                  version = Const.MSGP_VUK,
                  firmwareVersion = firmwareVersion
                )
            }
        }
      case _ =>
        throw new Exception("unsupported message pack")
    }
  }

  def unpackTimeseries(binData: Array[Byte]): Option[MsgPackMessageV2] = {

    val temps: mutable.Map[DateTime, Int] = mutable.HashMap.empty

    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
    val itr = unpacker.iterator()

    unpacker.getNextType match {
      case ValueType.ARRAY if itr.hasNext =>
        val va = itr.next().asArrayValue()

        val messageVersion = va.get(0).asRawValue().getString
        val firmwareVersion = va.get(1).asRawValue().getString

        val hwDeviceIdBytes = va.get(2).asRawValue().getByteArray
        //val byteBuffer = ByteBuffer.wrap(hwDeviceIdBytes)
        //val hwDeviceId = new UUID(byteBuffer.getLong(), byteBuffer.getLong())
        val hwDeviceIdHex = binary.Hex.encodeHexString(hwDeviceIdBytes)
        val hwDeviceId =
          hwDeviceIdHex.take(hwDeviceIdHex.length / 2) +
            "-" +
            hwDeviceIdHex.takeRight(hwDeviceIdHex.length / 2)

        val prevMessageHashBytes = va.get(3).asRawValue().getByteArray
        val prevMessageHash = if (prevMessageHashBytes.nonEmpty)
          Some(binary.Hex.encodeHexString(prevMessageHashBytes))
        else
          None

        val data = va.get(4).asMapValue()
        val plList = data.keySet().toArray flatMap { plKey =>
          val timestamp = new DateTime(JavaLong.parseLong(plKey.toString) * 1000, DateTimeZone.UTC)
          val timestampStr = timestamp.toDateTimeISO.toString
          val plVal = data.get(plKey)
          plVal.getType match {
            case ValueType.MAP =>
              val plMap = plVal.asMapValue()
              val ma = plMap.keySet().toArray.toList.foldLeft(Map[String, JValue]()) { (m, plKey) =>
                val mapValue = processScalarValue(plMap.get(plKey))
                m ++ Map[String, JValue](plKey.toString.replace("\"", "") -> mapValue.get)
              }
              List(ma ++ Map[String, JValue]("ts" -> JString(timestampStr)))
            case ValueType.ARRAY =>
              va.getElementArray map { av =>
                processScalarValue(av)
              } map (jv => createTempPaylodObject(timestampStr, jv.get)) toList
            case _ =>
              processScalarValue(plVal) match {
                case Some(jv) =>
                  List(createTempPaylodObject(timestampStr, jv))
                case _ =>
                  List()
              }
          }
        }

        val payload = Json4sUtil.any2jvalue(plList) match {
          case Some(jv) => jv
          case None => JNothing
        }

        val error = va.get(5).asIntegerValue().getInt
        val sigData = va.get(6).asRawValue().getByteArray

        val signature = Hex.encodeHexString(sigData)

        Some(MsgPackMessageV2(
          messageVersion = messageVersion,
          firmwareVersion = firmwareVersion,
          hwDeviceId = hwDeviceId,
          payloadJson = payload,
          payloadBin = sigData,
          prevMessageHash = prevMessageHash,
          errorCode = error,
          signature = Some(signature)
        ))
      case _ =>
        None
    }
  }

  /**
    * Calliope MsgPack
    *
    * @return
    */
  def unpackSingleValue(binData: Array[Byte]): Set[MsgPackMessage] = {
    val data: mutable.Set[MsgPackMessage] = mutable.Set.empty
    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))

    if (unpacker.getNextType == ValueType.INTEGER)
      unpacker.readInt() match {
        case 0 =>
          val cd = processMessage(unpacker)
          if (cd.isDefined)
            data.add(cd.get)
        case 1 =>
          val cd = processSigendMessage(unpacker)
          if (cd.isDefined)
            data.add(cd.get)
        case _ =>
          logger.error("unsupported message type")
      }
    data.toSet
  }

  private def createTempPaylodObject(timestamp: String, jvalue: JValue): JObject = {
    JObject(JField("t", jvalue),
      JField("ts", JString(timestamp)))
  }

  private def processMessage(unpacker: Unpacker): Option[MsgPackMessage] = {
    var currentId: Int = 0
    var cd: Option[MsgPackMessage] = None
    val itr = unpacker.iterator()
    while (itr.hasNext) {
      val v = itr.next()
      v.getType match {
        case ValueType.INTEGER =>
          currentId = v.asIntegerValue().intValue()
        case ValueType.RAW =>
          val payStr = v.asRawValue().getString
          Json4sUtil.string2JValue(payStr) match {
            case Some(p) =>
              val currentIdHex = Hex.encodeHexString(com.google.common.primitives.Ints.toByteArray(currentId))
              cd = Some(
                MsgPackMessage(
                  messageType = 1,
                  deviceId = currentIdHex,
                  payloadJson = p
                )
              )
            case None =>
              logger.error(s"invalid payload data: ${
                v.asRawValue().getString
              }")
          }
        case _ =>
          logger.error(s"invalid msgPack data: ${
            v.getType
          }")
      }
    }
    cd
  }

  private def processSigendMessage(unpacker: Unpacker): Option[MsgPackMessage] = {
    var currentId: Int = 0
    var cd: Option[MsgPackMessage] = None
    try {
      val itr = unpacker.iterator()
      while (itr.hasNext) {
        val v = itr.next()
        v.getType match {
          case ValueType.INTEGER =>
            currentId = v.asIntegerValue().intValue()
          case ValueType.RAW =>
            val dat = v.asRawValue().getByteArray
            val sig = dat.slice(0, 64)
            val payBin = dat.slice(64, dat.length)
            val payStr = new String(payBin, "ASCII")
            Json4sUtil.string2JValue(payStr) match {
              case Some(payJson) =>
                val currentIdHex = Hex.encodeHexString(com.google.common.primitives.Ints.toByteArray(currentId))
                cd = Some(MsgPackMessage(
                  messageType = 1,
                  deviceId = currentIdHex,
                  payloadJson = payJson,
                  payloadBin = Some(payBin),
                  signature = Some(Base64.getEncoder.encodeToString(sig))
                ))
              case None =>
                logger.error(s"invalid payload data")
            }
          case _ =>
            logger.error(s"invalid msgPack data: ${
              v.getType
            }")
        }
      }
    }
    catch {
      case e: Exception =>
        logger.error("error while processing processSigendMessage")
    }
    cd
  }

  private def processScalarValue(scalarValue: Value): Option[JValue] = scalarValue.getType match {
    case ValueType.INTEGER if scalarValue.asIntegerValue().isIntegerValue =>
      Some(JInt(scalarValue.asIntegerValue().getInt))
    case ValueType.INTEGER if !scalarValue.asIntegerValue().isIntegerValue =>
      Some(JInt(scalarValue.asIntegerValue().getInt))
    case ValueType.FLOAT =>
      Some(JDouble(scalarValue.asFloatValue().getDouble))
    case ValueType.RAW =>
      Some(JString(scalarValue.asRawValue().getString))
    case ValueType.BOOLEAN =>
      Some(JBool(scalarValue.asBooleanValue().getBoolean))
    case _ =>
      None
  }
}
