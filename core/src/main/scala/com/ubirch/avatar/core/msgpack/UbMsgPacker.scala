package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.util.uuid.UUIDUtil
import org.apache.commons.codec.binary.Hex
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST._
import org.json4s.jackson.JsonMethods.{compact, render}
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.{MapValue, Value, ValueType}

object UbMsgPacker extends StrictLogging {


  def processUbirchprot(binData: Array[Byte]): Unit = {
    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
    val itr = unpacker.iterator()
    if (itr.hasNext)
      processUbirchprot(itr.next())
  }

  def processUbirchprot(v: Value) = {
    val va = v.asArrayValue()
    val version = va.get(0).asIntegerValue().getInt
    val mainVersion = version >> 4
    val subVersion = version & 15

    val rawUuid = va.get(1).asRawValue().getByteArray
    val uuid = UUIDUtil.fromByteArray(rawUuid)
    println(s"uuid $uuid")

    println(s"main version $mainVersion")

    subVersion match {
      case 1 =>
        println("format v1")

        val messageType = va.get(2).asIntegerValue().getInt
        println(s"messageType $messageType")

        val payload = va.get(3)
        println(s"payload type: ${payload.getType}")


      case 2 =>
        println("format v2")

        val messageType = va.get(2).asIntegerValue().getInt
        println(s"messageType $messageType")

        val payload = va.get(3)
        println(s"payload type: ${payload.getType}")


        val rawSignature = va.get(4).asRawValue().getByteArray
        val signature = Hex.encodeHexString(rawSignature)
        println(s"signture $signature")

      case 3 =>
        println("format v3")

        val rawPrevHash = va.get(2).asRawValue().getByteArray
        val prevHash = Hex.encodeHexString(rawPrevHash)
        println(s"prevHash $prevHash")

        val messageType = va.get(3).asIntegerValue().getInt
        println(s"messageType $messageType")

        val payload = va.get(4)
        println(s"payload type: ${payload.getType}")

        processPayload(messageType, payload)

        val rawSignature = va.get(5).asRawValue().getByteArray
        val signature = Hex.encodeHexString(rawSignature)
        println(s"signture $signature")

      case _ =>
        println("new format ??")
    }
  }


  private def processPayload(messageType: Int, payload: Value) = {
    messageType match {
      case 83 =>
        throw new Exception("not implemented ubirch protocol T85")
      case 84 =>
        processT84Payload(payload)
      case 85 =>
        throw new Exception("not implemented ubirch protocol T85")
      case n: Int =>
        new Exception(s"unsupported msg type $n")
    }
  }

  private def processT84Payload(payload: Value) = {
    if (payload.asArrayValue().size() == 5) {
      println("playload size OK")
      val payArr = payload.asArrayValue()
      val version = payArr.get(0).asRawValue().getString
      val wakeups = payArr.get(1).asIntegerValue().getLong
      val status = payArr.get(2).asIntegerValue().getLong
      val mMap = payArr.get(3).asMapValue()
      val cMap = payArr.get(4).asMapValue()
      println(s"v: $version / w: $wakeups / s: $status")
      parseMeasurementMap(mMap)
      parseConfigMap(cMap)
    }
    else
      println("playload size not OK")
  }

  private def parseMeasurementMap(mVal: MapValue): JValue = {
    val res = mVal.keySet.toArray.map { key =>
      val curVal = mVal.get(key)
      val tsMillis = key.toString.toLong * 1000
      val ts = new DateTime(tsMillis, DateTimeZone.UTC)
      curVal.getType match {
        case ValueType.INTEGER =>
          val curValVal = curVal.asIntegerValue().getLong
          println(s"k: $ts ($key) -> v: $curValVal")
          Some((ts.toString -> JLong(curValVal)))
        case ValueType.RAW =>
          val curValVal = curVal.asRawValue().getString
          println(s"k: $ts ($key) -> v: $curValVal")
          Some((ts.toString -> JString(curValVal)))
        case ValueType.BOOLEAN =>
          val curValVal = curVal.asBooleanValue().getBoolean
          println(s"k: $ts ($key) -> v: $curValVal")
          Some((ts.toString -> JBool(curValVal)))
        case ValueType.FLOAT =>
          val curValVal = curVal.asFloatValue().getDouble
          println(s"k: $ts ($key) -> v: $curValVal")
          Some((ts.toString -> JDouble(curValVal)))
        case _ =>
          println("unsupported measurement type")
          None
      }
    }.filter(_.isDefined).map(_.get).toList
    val json = JObject(res)
    println(compact(render(json)))
    json
  }

  private def parseConfigMap(mVal: MapValue): JValue = {
    val res = mVal.keySet.toArray.map { key =>
      val keyStr = String.valueOf(key).replace("\"", "")
      val curVal = mVal.get(key)
      curVal.getType match {
        case ValueType.INTEGER =>
          val curValVal = curVal.asIntegerValue().getLong
          println(s"k: ${keyStr} ($key) -> v: $curValVal")
          Some((keyStr -> JLong(curValVal)))
        case ValueType.RAW =>
          val curValVal = curVal.asRawValue().getString
          println(s"k: $keyStr ($key) -> v: $curValVal")
          Some((keyStr -> JString(curValVal)))
        case _ =>
          println("unsupported config type")
          None
      }
    }.filter(_.isDefined).map(_.get).toList
    val json = JObject(res)
    println(compact(render(json)))
    json
  }

}
