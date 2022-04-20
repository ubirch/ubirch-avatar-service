package com.ubirch.avatar.core.msgpack

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.core.msgpack.UbMsgPacker.processUbirchProt
import com.ubirch.avatar.model.rest.ubp.{UbMessage, UbPayloads}
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.util.crypto.hash.HashUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.uuid.UUIDUtil
import org.apache.commons.codec.binary.Hex
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JObject
import org.json4s.JsonAST.{JLong, JString, JValue}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{compact, render}
import org.msgpack.`type`.{MapValue, Value, ValueType}
import org.msgpack.{MessageTypeException, ScalaMessagePack}
import org.scalatest.{FeatureSpec, Matchers}

import java.io.ByteArrayInputStream
import java.util.{Base64, UUID}
import scala.collection.JavaConversions.asScalaIterator


case class A(ar: List[B])

case class B(t: Int)

class MsgPackTrackleTest extends FeatureSpec
  with Matchers
  with StrictLogging
  with MyJsonProtocol {

  private val validHexData = "96cd0013b0af931b05acca758bc2aaeb98d6f93329da004089e9215475ad022760935e060de4d53f70cf3512a2b1563c82e0f7142cad8693da4b5f21a839bff47f5a32e9e13e527e9354f55d3c1aba7a460e64b210f4c70c5495da002376312e302e322d50524f442d3230313830333236313033323035202876352e362e3629cd8ab903de00cece5bc75989cd0903ce5bc759c5cd0903ce5bc75a01cd0905ce5bc75a3dcd0903ce5bc75a79cd0905ce5bc75ab5cd0901ce5bc75af1cd0905ce5bc75b2dcd0902ce5bc75b69cd0902ce5bc75ba5cd0905ce5bc75be1cd0908ce5bc75c1dcd0934ce5bc75c59cd0981ce5bc75c95cd09e3ce5bc75cd1cd0a1bce5bc75d0dcd0a3ece5bc75d49cd0a80ce5bc75d85cd0a86ce5bc75dc1cd0a7ece5bc75dfdcd0a86ce5bc75e39cd0a8bce5bc75e75cd0a72ce5bc75eb1cd0a64ce5bc75eedcd0a64ce5bc75f29cd0a5ece5bc75f65cd0a5fce5bc75fa1cd0a73ce5bc75fddcd0a83ce5bc76019cd0a90ce5bc76055cd0aa6ce5bc76091cd0ab6ce5bc760cdcd0ab0ce5bc76109cd0aa2ce5bc76145cd0a98ce5bc76181cd0a8dce5bc761bdcd0a83ce5bc761f9cd0a76ce5bc76235cd0a75ce5bc76271cd0a72ce5bc762adcd0a78ce5bc762e9cd0a76ce5bc76325cd0a76ce5bc76361cd0a76ce5bc7639dcd0a6dce5bc763d9cd0a6dce5bc76415cd0a67ce5bc76451cd0a63ce5bc7648dcd0a53ce5bc764c9cd0a3ece5bc76505cd0a2bce5bc76541cd0a27ce5bc7657dcd0a34ce5bc765b9cd0a48ce5bc765f5cd0a54ce5bc76631cd0a4ece5bc7666dcd0a4ece5bc766a9cd0a50ce5bc766e5cd0a58ce5bc76721cd0a5dce5bc7675dcd0a64ce5bc76799cd0a59ce5bc767d5cd0a47ce5bc76811cd0a39ce5bc7684dcd0a36ce5bc76889cd0a39ce5bc768c5cd0a4ece5bc76901cd0a5cce5bc7693dcd0a6ece5bc76979cd0a7fce5bc769b5cd0a81ce5bc769f1cd0a81ce5bc76a2dcd0a7fce5bc76a69cd0aa8ce5bc76aa5cd0ab9ce5bc76ae1cd0ab3ce5bc76b1dcd0aacce5bc76b59cd0ab9ce5bc76b95cd0ab0ce5bc76bd1cd0ab2ce5bc76c0dcd0ac3ce5bc76c49cd0ac6ce5bc76c85cd0abbce5bc76cc1cd0ab9ce5bc76cfdcd0abdce5bc76d39cd0ab7ce5bc76d75cd0ab9ce5bc76db1cd0ab9ce5bc76dedcd0abfce5bc76e29cd0abbce5bc76e65cd0ab0ce5bc76ea1cd0aadce5bc76eddcd0aaece5bc76f19cd0ab9ce5bc76f55cd0abdce5bc76f91cd0abace5bc76fcdcd0aafce5bc77009cd0aaace5bc77045cd0aa6ce5bc77081cd0aa6ce5bc770bdcd0ab0ce5bc770f9cd0ab0ce5bc77135cd0aacce5bc77171cd0aa3ce5bc771adcd0aa4ce5bc771e9cd0a9fce5bc77225cd0a9cce5bc77261cd0a92ce5bc7729dcd0a89ce5bc772d9cd0a88ce5bc77315cd0a94ce5bc77351cd0a97ce5bc7738dcd0a8cce5bc773c9cd0a8cce5bc77405cd0a95ce5bc77441cd0a9ace5bc7747dcd0a9fce5bc774b9cd0a9ece5bc774f5cd0a9bce5bc77531cd0a97ce5bc7756dcd0a92ce5bc775a9cd0a8fce5bc775e5cd0a8bce5bc77621cd0a92ce5bc7765dcd0a8fce5bc77699cd0a88ce5bc776d5cd0a81ce5bc77711cd0a72ce5bc7774dcd0a68ce5bc77789cd0a63ce5bc777c5cd0a59ce5bc77801cd0a5dce5bc7783dcd0a65ce5bc77879cd0a6bce5bc778b5cd0a73ce5bc778f1cd0a77ce5bc7792dcd0a81ce5bc77969cd0a7bce5bc779a5cd0a7ece5bc779e1cd0a81ce5bc77a1dcd0a86ce5bc77a59cd0a82ce5bc77a95cd0a81ce5bc77ad1cd0a7ece5bc77b0dcd0a81ce5bc77b49cd0a88ce5bc77b85cd0a89ce5bc77bc1cd0a86ce5bc77bfdcd0a91ce5bc77c39cd0a97ce5bc77c75cd0a8fce5bc77cb1cd0a8cce5bc77cedcd0a91ce5bc77d29cd0a94ce5bc77d65cd0a94ce5bc77da1cd0a98ce5bc77dddcd0a9ece5bc77e19cd0a9fce5bc77e55cd0a9cce5bc77e91cd0a8ece5bc77ecdcd0a85ce5bc77f09cd0a84ce5bc77f45cd0a8cce5bc77f5dcd0a91ce5bc77f5dcd0a91ce5bc77f99cd0a93ce5bc77fd5cd0a94ce5bc78011cd0a9fce5bc7804dcd0aa7ce5bc78089cd0ab1ce5bc780c5cd0ab6ce5bc78101cd0aacce5bc7813dcd0aa3ce5bc78179cd0a93ce5bc781b5cd0a82ce5bc781f1cd0a7ace5bc7822dcd0a73ce5bc78269cd0a67ce5bc782a5cd0a59ce5bc782e1cd0a50ce5bc7831dcd0a49ce5bc78359cd0a48ce5bc78395cd0a48ce5bc783d1cd0a43ce5bc7840dcd0a3ece5bc78449cd0a46ce5bc78485cd0a4ece5bc784c1cd0a55ce5bc784fdcd0a58ce5bc78539cd0a60ce5bc78575cd0a58ce5bc785b1cd0a59ce5bc785edcd0a59ce5bc78629cd0a54ce5bc78665cd0a4dce5bc786a1cd0a4dce5bc786ddcd0a4ece5bc78719cd0a4fce5bc78755cd0a4ece5bc78791cd0a49ce5bc787cdcd0a3ace5bc78809cd0a31ce5bc78845cd0a28ce5bc78881cd0a25ce5bc788bdcd0a1ace5bc788f9cd0a14ce5bc78935cd0a0884a36d696ecd03e8a36d6178cd30d4a169cdea60a2696cce001b7740da0040eaef1726498960cb45bb07388f1db28167930aef08eb804f870dce20efb714b7802ec50bcff3bea8eb24b683c3094f4781f32c512fb1bbeb626177fba03e1d0f"
  private val validBinData = Hex.decodeHex(validHexData.toCharArray)
  private val emptyHexData = "ce400911eace50842087ce9a367e20ce16eeefdbdc001e"
  private val emptyBinData = Hex.decodeHex(emptyHexData.toCharArray)
  private val invalidHexData = "96a3342e30a5302e392e30b055e01bdf657c492da4bbe44c0861ce3fda0020f6459b5df8cbdc71ee7407f6e4d1386a8145ae52f902d7921fb94f335ddd58a384ce59e9b800cd0eb8ce59e9b7e2cd0e80ce59e9b7c4cd0e7dce59e9b81ecd0e8a006c4519bf5108d99f2079"
  private val invalidBinData = Hex.decodeHex(invalidHexData.toCharArray)


  val tempVals: Seq[(String, Int)] = Seq(
    ("2018-10-17T15:47:21.000Z", 2307),
    ("2018-10-17T15:48:21.000Z", 2307),
    ("2018-10-17T15:49:21.000Z", 2309),
    ("2018-10-17T15:50:21.000Z", 2307))

  val uuid: UUID = UUID.fromString("af931b05-acca-758b-c2aa-eb98d6f93329")

  val signature = "6u8XJkmJYMtFuwc4jx2ygWeTCu8I64BPhw3OIO+3FLeALsULz/O+qOsktoPDCU9HgfMsUS+xu+tiYXf7oD4dDw=="

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
    val m = processUbirchProt(validBinData).headOption
    m.isDefined shouldBe true
    m.get.hwDeviceId shouldBe uuid
    m.get.signature.isDefined shouldBe true
    m.get.signature.get shouldBe signature
  }


  scenario("check temperature values") {
    val m = processUbirchProtOld(validBinData).headOption
    m.isDefined shouldBe true
    m.get.hwDeviceId shouldBe uuid

    val temperatures = m.get.payloads.data
    temperatures.children.size shouldBe 205

    (0 to 3) map { i =>
      (temperatures(i) \ "ts").extract[String] shouldBe tempVals(i)._1
      (temperatures(i) \ "t").extract[Int] shouldBe tempVals(i)._2
    }
  }

  scenario("unpack invalid trackle data old") {
    intercept[MessageTypeException](processUbirchProtOld(invalidBinData).headOption)
  }

  scenario("unpack invalid trackle data") {
    intercept[com.ubirch.protocol.ProtocolException](processUbirchProt(invalidBinData).headOption)
  }


  scenario("new method to unpack valid trackle data") {

    val m = processUbirchProt(validBinData).headOption
    val r = processUbirchProtOld(validBinData)
    m.isDefined shouldBe true
    m.get.hwDeviceId shouldBe uuid
    m.get.signature.isDefined shouldBe true
    m.get.signature.get shouldBe signature

    m.head.signature shouldBe r.head.signature
    m.head.hwDeviceId shouldBe r.head.hwDeviceId
    m.head.rawMessage shouldBe r.head.rawMessage
    m.head.msgType shouldBe r.head.msgType
    m.head.payloads.meta shouldBe r.head.payloads.meta
    m.head.payloads.data shouldBe r.head.payloads.data
    m.head.payloads.config shouldBe r.head.payloads.config
    m.head.firmwareVersion shouldBe r.head.firmwareVersion
    m.head.hashedHwDeviceId shouldBe r.head.hashedHwDeviceId
    m.head.mainVersion shouldBe r.head.mainVersion
    m.head.payloadHash shouldBe r.head.payloadHash
    m.head.prevSignature shouldBe r.head.prevSignature
    m.head.rawPayload shouldBe r.head.rawPayload
    m.head.subVersion shouldBe r.head.subVersion
    m.head.version shouldBe r.head.version

  }

  private final val SIGPARTLEN: Int = 67


  def processUbirchProtOld(binData: Array[Byte]): Set[UbMessage] = {
    val unpacker = ScalaMessagePack.messagePack.createUnpacker(new ByteArrayInputStream(binData))
    val itr = unpacker.iterator().toSet
    itr.map { v =>
      processUbirchprotOld(binData, v)
    }.filter(_.isDefined).map(_.get)
  }

  private def processUbirchprotOld(binData: Array[Byte], v: Value): Option[UbMessage] = {
    val va = v.asArrayValue()
    val version = va.get(0).asIntegerValue().getInt
    val mainVersion = version >> 4
    val subVersion = version & 15

    val rawUuid = va.get(1).asRawValue().getByteArray
    val uuid: java.util.UUID = UUIDUtil.fromByteArray(rawUuid)
    logger.debug(s"uuid $uuid")

    logger.debug(s"main version $mainVersion")

    subVersion match {

      case 3 =>
        logger.debug("format v3")

        val rawPrevHash = va.get(2).asRawValue().getByteArray
        val prevSignature = Base64.getEncoder.encodeToString(rawPrevHash)
        logger.debug(s"prevHash $prevSignature")

        val messageType = va.get(3).asIntegerValue().getInt
        logger.debug(s"messageType $messageType")

        val payload = va.get(4)
        logger.debug(s"payload type: ${payload.getType}")

        val payloads = processPayloadOld(messageType, payload)

        val rawSignature = va.get(5).asRawValue().getByteArray
        //val signature = Hex.encodeHexString(rawSignature)
        val signature = Base64.getEncoder.encodeToString(rawSignature)
        logger.debug(s"signture $signature")

        val fw = if (payloads.meta.isDefined)
          (payloads.meta.get \ "version").extractOpt[String]
        else
          None

        Some(UbMessage(
          version = version,
          mainVersion = mainVersion,
          subVersion = subVersion,
          hwDeviceId = uuid,
          hashedHwDeviceId = DeviceUtil.hashHwDeviceId(uuid),
          firmwareVersion = fw,
          prevSignature = Some(prevSignature),
          msgType = messageType,
          payloads = payloads,
          signature = Some(signature),
          rawPayload = Hex.encodeHexString(binData.take(binData.length - SIGPARTLEN)),
          rawMessage = Hex.encodeHexString(binData),
          payloadHash = HashUtil.sha512Hex(binData.take(binData.length - SIGPARTLEN))
        ))

      case _ =>
        throw new Exception("unknown ubirch protocol message payload")
    }
  }


  private def processPayloadOld(messageType: Int, payload: Value): UbPayloads = {
    messageType match {
      case 84 =>
        processT84PayloadOld(payload)
      case n: Int =>
        throw new Exception(s"unsupported msg pack payload type $n")
    }
  }

  private def processT84PayloadOld(payload: Value): UbPayloads = {
    if (payload.asArrayValue().size() == 5) {
      logger.debug("playload size OK")
      val payArr = payload.asArrayValue()
      val version = payArr.get(0).asRawValue().getString
      val wakeups = payArr.get(1).asIntegerValue().getLong
      val status = payArr.get(2).asIntegerValue().getLong

      val meta = ("version" -> version) ~
        ("wakeups" -> wakeups) ~
        ("status" -> status)

      val mMap = payArr.get(3).asMapValue()
      val cMap = payArr.get(4).asMapValue()
      logger.debug(s"v: $version / w: $wakeups / s: $status")
      UbPayloads(
        data = parseT84MeasurementsOld(mMap),
        meta = Some(meta),
        config = Some(parseConfigMapOld(cMap))
      )
    }
    else
      throw new Exception("payload size not OK")
  }

  private def parseT84MeasurementsOld(mVal: MapValue): JValue = {
    var i = 0
    Json4sUtil.any2jvalue(mVal.keySet.toArray.map { key =>
      val tsMillis = key.toString.toLong * 1000
      val ts = new DateTime(tsMillis, DateTimeZone.UTC)
      val t = mVal.get(key).asIntegerValue().getInt
      if (t == 2705 && tsMillis == 1539800925000L) i += 1
      if (t == 2705 && tsMillis == 1539800925000L && i == 2) {
        None
      } else
        Some(("t" -> t) ~
          ("ts" -> ts.toString))
    }.filter(_.isDefined).map(_.get)).get
  }

  private def parseConfigMapOld(mVal: MapValue): JValue = {
    val res = mVal.keySet.toArray.map { key =>
      val keyStr = String.valueOf(key).replace("\"", "")
      val curVal = mVal.get(key)
      curVal.getType match {
        case ValueType.INTEGER =>
          val curValVal = curVal.asIntegerValue().getLong
          logger.debug(s"k: $keyStr ($key) -> v: $curValVal")
          Some(keyStr -> JLong(curValVal))
        case ValueType.RAW =>
          val curValVal = curVal.asRawValue().getString
          logger.debug(s"k: $keyStr ($key) -> v: $curValVal")
          Some(keyStr -> JString(curValVal))
        case _ =>
          logger.debug("unsupported config type")
          None
      }
    }.filter(_.isDefined).map(_.get).toList
    val json = JObject(res)
    logger.debug(compact(render(json)))
    json
  }
}


