package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream
import java.lang.{Long => JavaLong}
import java.util.Base64

import com.google.common.primitives.Ints
import com.typesafe.scalalogging.slf4j.StrictLogging
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

object MsgPacker extends StrictLogging {

  def unpackTimeseries(binData: Array[Byte]): Option[MsgPackMessageV2] = {

    val temps: mutable.Map[DateTime, Int] = mutable.HashMap.empty

    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
    val itr = unpacker.iterator()

    unpacker.getNextType() match {
      case ValueType.ARRAY if itr.hasNext() =>
        val va = itr.next().asArrayValue()

        val messageVersion = va.get(0).asRawValue().getString
        val firmwareVersion = va.get(1).asRawValue().getString

        val hwDeviceIdBytes = va.get(2).asRawValue().getByteArray
        //val byteBuffer = ByteBuffer.wrap(hwDeviceIdBytes)
        //val hwDeviceId = new UUID(byteBuffer.getLong(), byteBuffer.getLong())
        val hwDeviceIdHex = binary.Hex.encodeHexString(hwDeviceIdBytes)
        val hwDeviceId =
          hwDeviceIdHex.take(hwDeviceIdHex.size / 2) +
            "-" +
            hwDeviceIdHex.takeRight(hwDeviceIdHex.size / 2)

        val prevMessageHashBytes = va.get(3).asRawValue().getByteArray
        val prevMessageHash = if (prevMessageHashBytes.size > 0)
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
          payload = payload,
          prevMessageHash = prevMessageHash,
          errorCode = error,
          signature = Some(signature)
        ))
      case _ =>
        None
    }
  }

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
              val currentIdHex = Hex.encodeHexString(Ints.toByteArray(currentId))
              cd = Some(
                MsgPackMessage(
                  messageType = 1,
                  deviceId = currentIdHex,
                  payload = p
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

  private def processSigendMessage(unpacker: Unpacker): Option[MsgPackMessage]

  = {
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
            val pay = dat.slice(64, dat.length)
            val payStr = new String(pay, "UTF-8")
            Json4sUtil.string2JValue(payStr) match {
              case Some(p) =>
                val currentIdHex = Hex.encodeHexString(Ints.toByteArray(currentId))
                cd = Some(MsgPackMessage(
                  messageType = 1,
                  deviceId = currentIdHex,
                  payload = p,
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
