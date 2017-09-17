package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream
import java.util.Base64

import com.google.common.primitives.Ints
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.model.rest.device.MsgPackMessage
import com.ubirch.util.json.Json4sUtil
import org.apache.commons.codec.binary.Hex
import org.joda.time.{DateTime, DateTimeZone}
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.ValueType
import org.msgpack.unpacker.Unpacker

import scala.collection.mutable

object MsgPacker extends StrictLogging {

  def unpackTimeseries(binData: Array[Byte]) = {

    val ids: mutable.ArrayBuffer[Long] = mutable.ArrayBuffer.empty
    val temps: mutable.Map[DateTime, Int] = mutable.HashMap.empty

    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
    val itr = unpacker.iterator()
    var done = false
    while (itr.hasNext && !done) {
      val v = itr.next()
      v.getType match {
        case ValueType.INTEGER =>
          val value = v.asIntegerValue.getLong
          ids.append(value)
        case ValueType.MAP =>
          val map = v.asMapValue()
          val keys = map.keySet()
          val kItr = keys.iterator()
          while (kItr.hasNext) {
            val key = kItr.next()
            val dt = new DateTime(key.asIntegerValue().longValue() * 1000, DateTimeZone.UTC)
            val temp = map.get(key)
            temps.update(key = dt, value = temp.asIntegerValue().getInt)
          }
          done = true
        case _ =>
      }
    }
    if (ids.size.equals(4)) {
      val did = ids.mkString("-")
      logger.info(s"deviceId: $did / t: $temps")
      (did, temps.toMap)
    }
    else
      throw new Exception("invalid data")
  }

  def unpackSingleValue(binData: Array[Byte]): Set[MsgPackMessage] = {
    val data: mutable.Set[MsgPackMessage] = mutable.Set.empty
    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
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
              logger.error(s"invalid payload data: ${v.asRawValue().getString}")
          }
        case _ =>
          logger.error(s"invalid msgPack data: ${v.getType}")
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
            logger.error(s"invalid msgPack data: ${v.getType}")
        }
      }
    }
    catch {
      case e: Exception =>
        logger.error("error while processing processSigendMessage")
    }
    cd
  }
}
