package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import org.apache.commons.codec.binary.Hex
import org.json4s.JsonAST.JArray
import org.msgpack.ScalaMessagePack
import org.scalatest.{FeatureSpec, Matchers}

class MsgPackTrackleTest extends FeatureSpec
  with Matchers
  with StrictLogging
  with MyJsonProtocol {

  private val validHexData = "ce400911eace50842087ce9a367e20ce16eeefdbdc001ecd0e38cd0ec4cd0e9ccd0ececd0e91cd0e91cd0ececd0e91cd0e57cd0ececd0e91cd0e91cd0e91cd0e9ccd0ececd0ec8cd0ec4cd0e91cd0ececd0e9ccd0e38cd0e16cd0ec4cd0e73cd0ececd0e57cd0e44cd0e65cd0e44cd0e38ce400911eace50842087ce9a367e20ce16eeefdb96cd0e73cd0e16cd0e9ccd0e16cd0e16cd0ece0d0a0d0a"
  private val validBinData = Hex.decodeHex(validHexData.toCharArray)
  private val emptyHexData = "ce400911eace50842087ce9a367e20ce16eeefdbdc001e"
  private val emptyBinData = Hex.decodeHex(emptyHexData.toCharArray)
  private val invalidHexData = "ce400911eace50842087ce16eeefdbdc001ecd0e38cd0ec4cd0e9ccd0ececd0e91cd0e91cd0ececd0e91cd0e57cd0ececd0e91cd0e91cd0e91cd0e9ccd0ececd0ec8cd0ec4cd0e91cd0ececd0e9ccd0e38cd0e16cd0ec4cd0e73cd0ececd0e57cd0e44cd0e65cd0e44cd0e38ce400911eace50842087ce9a367e20ce16eeefdb96cd0e73cd0e16cd0e9ccd0e16cd0e16cd0ece0d0a0d0a"
  private val invalidBinData = Hex.decodeHex(invalidHexData.toCharArray)

  val uid1 = 1074336234l
  val uid2 = 1350836359l
  val uid3 = 2587262496l
  val uid4 = 384757723l

  val tempVals = Seq[Int](3640, 3780, 3740, 3790, 3729, 3729, 3790, 3729, 3671, 3790, 3729, 3729, 3729, 3740, 3790, 3784, 3780, 3729, 3790, 3740, 3640, 3606, 3780, 3699, 3790, 3671, 3652, 3685, 3652, 3640)

  val uid = s"$uid1-$uid2-$uid3-$uid4"

  feature("MsgPack") {

    scenario("unpack valid data") {

      val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(validBinData))

      unpacker.readBigInteger().longValue() shouldBe uid1
    }

    scenario("unpack valid trackle data") {
      val (u, t) = MsgPacker.unpackTimeseries(validBinData)
      u shouldBe uid
      t shouldBe tempVals
    }

    scenario("unpack empty trackle data") {
      val (u, t) = MsgPacker.unpackTimeseries(emptyBinData)
      u shouldBe uid
      t shouldBe Seq[Int]()
    }

    scenario("unpack invalid trackle data") {
      intercept[Exception] {
        val (u, t) = MsgPacker.unpackTimeseries(invalidBinData)
      }
    }

    scenario("create DeviceRawData") {
      val (u, t) = MsgPacker.unpackTimeseries(validBinData)

      val drd = DeviceDataRawManager.create(did = u, vals = t, mpraw = validBinData).get
      val pJsonStr = Json4sUtil.jvalue2String(drd.p)

      drd.did shouldBe Some(uid)
      drd.a shouldBe HashUtil.sha512Base64(uid)
      val pArray = drd.p.extract[JArray]
      pArray.values.size shouldBe tempVals.size
    }

  }

}
