package com.ubirch.avatar.core.msgpack

import com.typesafe.scalalogging.slf4j.StrictLogging
import org.apache.commons.codec.binary.Hex
import org.msgpack.`type`.Value

object UbMsgPacker extends StrictLogging{

  private def processUbirchprot(v: Value) = {
    val va = v.asArrayValue()
    val version = va.get(0).asIntegerValue().getInt
    val mainVersion = version >> 4
    val subVersion = version & 15

    val rawUuid = va.get(1).asRawValue().getByteArray
    val uuid = io.jvm.uuid.UUID.fromByteArray(rawUuid, offset = 0)
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

}
