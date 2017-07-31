package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.joda.time.{DateTime, DateTimeZone}
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.ValueType

import scala.collection.mutable

object MsgPacker extends StrictLogging {

  def unpack(binData: Array[Byte]) = {

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

}
