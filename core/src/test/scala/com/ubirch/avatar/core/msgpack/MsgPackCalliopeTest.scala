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

    //    scenario("unpack valid trackle data") {
    //      val (u, t) = MsgPacker.unpackTrackle(validBinData)
    //      u shouldBe uid
    //      t shouldBe tempVals
    //    }
    //
    //    scenario("unpack empty trackle data") {
    //      val (u, t) = MsgPacker.unpackTrackle(emptyBinData)
    //      u shouldBe uid
    //      t shouldBe Seq[Int]()
    //    }
    //
    //    scenario("unpack invalid trackle data") {
    //      intercept[Exception] {
    //        val (u, t) = MsgPacker.unpackTrackle(invalidBinData)
    //      }
    //    }
    //
    //    scenario("create DeviceRawData") {
    //      val (u, t) = MsgPacker.unpackTrackle(validBinData)
    //
    //      val drd = DeviceDataRawManager.create(did = u, vals = t, mpraw = validBinData).get
    //      val pJsonStr = Json4sUtil.jvalue2String(drd.p)
    //
    //      drd.did shouldBe Some(uid)
    //      drd.a shouldBe HashUtil.sha512Base64(uid)
    //      val pArray = drd.p.extract[JArray]
    //      pArray.values.size shouldBe tempVals.size
    //    }

  }

}
