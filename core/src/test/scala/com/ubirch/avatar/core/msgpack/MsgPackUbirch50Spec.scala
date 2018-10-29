package com.ubirch.avatar.core.msgpack

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.model.rest.ubp.{UbMessage, UbPayloads}
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import org.apache.commons.codec.binary.Hex
import org.scalatest.{FeatureSpec, Matchers}

class MsgPackUbirch50Spec
  extends FeatureSpec
    with Matchers
    with StrictLogging
    with MyJsonProtocol {

  val testMsgs = List(
    List("96cd0013b030aea423496812233445566778899aabda0040eaa430f241dd5f40fd055ac85842629c046482b99ae1ce5dd747864872ca9834b88847590b18300735b6598f5798d2ba18b9ada980a135747eb9c1b8705285083292cf000577f7638c438003da004029d701c401b35052521a4cb7ef5d5218dd49dfc2cb7dc9e69f2b32b46e22d16907c7005c2495cdd31969916138949d3dcfd08f6714d7c1a2aa86ff495882d901", """{"ts":1539279294317,"valueA":3}"""),
    List("9613b0d3844deb370548f6a3059e2993fd3bfada0040bbe9c49ceac6639d2ac2852ff417bec1906d5492f9cda1fe144c1758385c743420574d95f1fabfb3e813011ef8042c6d04446b62f74739b4364dfbe8333c19073292cd04d2ce0006f855da00400b1316027b297699128835693915bae30c26e631818ba5bd6cbc0efd3f40396fbd5eaf07395f0f36371a29672fe8d4ffb39d5d7f1d003c6f4e0d9512f940c008", """{"ts":1,"valueA":456789}"""),
    List("9613b0d3844deb370548f6a3059e2993fd3bfada00400b1316027b297699128835693915bae30c26e631818ba5bd6cbc0efd3f40396fbd5eaf07395f0f36371a29672fe8d4ffb39d5d7f1d003c6f4e0d9512f940c0083293cf00057840b555dd672acd0539da00409dcaf8b0c7edd69e331f4ec3bc35a38301becf6f6a3c174077e47a4db4beddbc8bbca97d7a68e7af976f02caa9df29d8000ee70975a0df2df64e8603d7b31001", """{"ts":1539594199096,"valueA":42,"valueB":1337}""")
  )

  feature("MsgPack ubirch 0x32") {
    scenario("basic parsing") {
      testMsgs.map { data =>
        val testMsgBin = Hex.decodeHex(data.head)
        val testJson = data.tail.head
        val result: Set[UbMessage] = UbMsgPacker.processUbirchprot(testMsgBin)
        result.size shouldBe 1
        val jsonStr = Json4sUtil.jvalue2String(result.toList.head.payloads.data).trim
        logger.info(jsonStr)
        jsonStr shouldBe testJson
      }
    }

    scenario("type 0x53") {
      val data = List( "9512b0000000000000000000000000000000005382a27473ce5bcee253a17601da0040a3ba4ae742e29d57e54454653621c14a6b0fc6bd0d8e1cd15483a2f213c2df95ad6c20566e042ccab4893c2f75af30e984e5e272de597a12bb391d179f937d0c","""{"ts":1540285011,"v":1}"""
      )

      val testMsgBin = Hex.decodeHex(data.head)
      val testJson = data.last
      val result: Set[UbMessage] = UbMsgPacker.processUbirchprot(testMsgBin)
      result.size shouldBe 1
      val jsonStr = Json4sUtil.jvalue2String(result.toList.head.payloads.data).trim
      logger.info(jsonStr)
      jsonStr shouldBe testJson
    }

  }
}
