package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.ValueType

import scala.collection.mutable

object MsgPacker extends StrictLogging {

  def unpack(binData: Array[Byte]) = {

    val ids: mutable.ArrayBuffer[Long] = mutable.ArrayBuffer.empty
    val temps: mutable.ArrayBuffer[Int] = mutable.ArrayBuffer.empty

    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
    val itr = unpacker.iterator()
    var done = false
    while (itr.hasNext && !done) {
      val v = itr.next()
      v.getType match {
        case ValueType.INTEGER =>
          val value = v.asIntegerValue.getLong
          ids.append(value)
        case ValueType.ARRAY =>
          val arr = v.asArrayValue()
          val itr2 = arr.iterator()
          while (itr2.hasNext) {
            val tval = itr2.next().asIntegerValue().getInt
            temps.append(tval)
          }
          done = true
        case _ =>
      }
    }
    if (ids.size.equals(4)) {
      val did = ids.mkString("-")
      val ts = temps.toSeq

      logger.info(s"deviceId: $did / t: $ts")

      (did, ts)
    }
    else
      throw new Exception("invalid data")
  }

}
