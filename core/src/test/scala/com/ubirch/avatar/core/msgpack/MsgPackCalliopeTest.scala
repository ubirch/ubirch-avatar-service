package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream

import com.google.common.primitives.{Ints, Longs}
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import org.apache.commons.codec.binary.Hex
import org.json4s.JsonAST.JArray
import org.msgpack.ScalaMessagePack
import org.scalatest.{FeatureSpec, Matchers}

class MsgPackCalliopeTest extends FeatureSpec
  with Matchers
  with StrictLogging
  with MyJsonProtocol {

  private val validHexData = "cebc9ab239ac7b2274657374223a3132337dcebc9ab239b47b2274657374223a2276616c756520313233227d"
  private val validBinData = Hex.decodeHex(validHexData.toCharArray)

  val did1 = "bc9AB239"


  feature("MsgPack") {

    scenario("unpack valid data") {

      val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(validBinData))

      val deviceId = unpacker.iterator().next().asIntegerValue().intValue()
      val deviceIdHex = Hex.encodeHexString(Ints.toByteArray(deviceId))

      deviceIdHex shouldBe did1.toLowerCase
    }

    scenario("unpack calliope data") {
      val cData = MsgPacker.unpackCalliope(validBinData)
      cData.size shouldBe 2

      cData.head.deviceId shouldBe did1.toLowerCase
      val data = (cData.head.payload \ "test").extract[String]
      data shouldBe "value 123"
    }
  }
}
