package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream

import com.google.common.primitives.{Ints, Longs}
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.core.protocol.msgpack.MsgPacker
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

  private val validHexData = "01ce194e8f56d94d7250bd00430479c39326d8ce9f67cb7ab1e0f40fb81495c26498902db100eed5cdc62afe981a82e1eeb2f8ca4f999a9e16a979d5d4682da833b9a42193b9340d7b226c69676874223a3131317d"
  private val validBinData = Hex.decodeHex(validHexData.toCharArray)

  val did1 = "194e8f56"


  feature("MsgPack") {

    scenario("unpack valid data") {

      val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(validBinData))

      val marker = unpacker.iterator().next().asIntegerValue().intValue()

      marker shouldBe 1
    }

    scenario("unpack calliope data") {
      val mpMsgs = MsgPacker.unpackSingleValue(validBinData)
      mpMsgs.size shouldBe 1

      mpMsgs.head.deviceId shouldBe did1.toLowerCase
      val data = (mpMsgs.head.payloadJson \ "light").extractOpt[Int]
      data.isDefined shouldBe true
      data.get shouldBe 111
    }
  }
}
