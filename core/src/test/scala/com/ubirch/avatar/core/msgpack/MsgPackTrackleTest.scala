package com.ubirch.avatar.core.msgpack

import java.io.ByteArrayInputStream

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.core.protocol.msgpack.MsgPacker
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import org.apache.commons.codec.binary.Hex
import org.json4s.JValue
import org.json4s.native.Serialization.{read, write}
import org.msgpack.ScalaMessagePack
import org.msgpack.`type`.ValueType
import org.scalatest.{FeatureSpec, Matchers}

case class A(ar: List[B])

case class B(t: Int)

class MsgPackTrackleTest extends FeatureSpec
  with Matchers
  with StrictLogging
  with MyJsonProtocol {

  private val validHexData = "96a3342e30a5302e392e30b055e01bdf657c492da4bbe44c0861ce3fda0020f6459b5df8cbdc71ee7407f6e4d1386a8145ae52f902d7921fb94f335ddd58a384ce59e9b800cd0eb8ce59e9b7e2cd0e80ce59e9b7c4cd0e7dce59e9b81ecd0e8a006c4519bf5108d99f20796cc613ef6266089f6e5f610ea30c37f0d6b982caf709ec2a1cd3f3f4dbdb7b285cf874208f0c4cb3c88ba34fe10c5b4cf15c4d778706"
  private val validBinData = Hex.decodeHex(validHexData.toCharArray)
  private val emptyHexData = "ce400911eace50842087ce9a367e20ce16eeefdbdc001e"
  private val emptyBinData = Hex.decodeHex(emptyHexData.toCharArray)
  private val invalidHexData = "96a3342e30a5302e392e30b055e01bdf657c492da4bbe44c0861ce3fda0020f6459b5df8cbdc71ee7407f6e4d1386a8145ae52f902d7921fb94f335ddd58a384ce59e9b800cd0eb8ce59e9b7e2cd0e80ce59e9b7c4cd0e7dce59e9b81ecd0e8a006c4519bf5108d99f2079"
  private val invalidBinData = Hex.decodeHex(invalidHexData.toCharArray)


  val tempVals = Seq[Int](3768, 3712, 3709, 3722)

  val uid = "55e01bdf657c492da4bbe44c0861ce3f"

  val signature = "6c4519bf5108d99f20796cc613ef6266089f6e5f610ea30c37f0d6b982caf709ec2a1cd3f3f4dbdb7b285cf874208f0c4cb3c88ba34fe10c5b4cf15c4d778706"

  feature("MsgPack") {

    val p = A(
      ar = List[B](
        B(3),
        B(5),
        B(7)
      )
    )

    val jval = Json4sUtil.any2jvalue(p)

    jval.isDefined shouldBe true
  }

  scenario("unpack valid data") {

    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(validBinData))

    unpacker.getNextType shouldBe ValueType.ARRAY
  }

  scenario("unpack valid trackle data") {
    val m = MsgPacker.unpackTimeseries(validBinData)
    m.isDefined shouldBe true
    m.get.hwDeviceId shouldBe uid
    m.get.signature.isDefined shouldBe true
    m.get.signature.get shouldBe signature
  }

  scenario("check temp values") {
    val m = MsgPacker.unpackTimeseries(validBinData)
    m.isDefined shouldBe true
    m.get.hwDeviceId shouldBe uid

    var plvs = m.get.payloadJson.children.toArray
    (1 to 4) map { i =>
      tempVals.size >= i shouldBe true
      plvs.size >= i shouldBe true
      (plvs(i - 1) \ "t").extractOpt[Int].isDefined shouldBe true
      tempVals(i - 1) shouldBe (plvs(i - 1) \ "t").extract[Int]
    }

    m.get.payloadJson.children.foreach { p =>
      (p \ "t").extractOpt[Int].isDefined shouldBe true
      tempVals.contains((p \ "t").extract[Int]) shouldBe true
      (p \ "ts").extractOpt[String].isDefined shouldBe true
    }
  }

  scenario("unpack empty trackle data") {
    val m = MsgPacker.unpackTimeseries(invalidBinData)
    m.isEmpty shouldBe true
  }

  scenario("unpack invalid trackle data") {
    val m = MsgPacker.unpackTimeseries(invalidBinData)
    m.isEmpty shouldBe true
  }
}


