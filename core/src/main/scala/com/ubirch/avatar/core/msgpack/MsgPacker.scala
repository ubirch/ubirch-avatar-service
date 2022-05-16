package com.ubirch.avatar.core.msgpack

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Const
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.ValueType

import java.io.ByteArrayInputStream
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
            MsgPackVersion(
              version = Const.MSGP_V41,
              firmwareVersion = "unknown"
            )
          case _ =>
            throw new Exception("unsupported message pack")
        }
      case _ =>
        throw new Exception("unsupported message pack")
    }
  }

}