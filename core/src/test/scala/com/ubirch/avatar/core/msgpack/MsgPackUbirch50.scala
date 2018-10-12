package com.ubirch.avatar.core.msgpack

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.model.rest.ubp.{UbMessage, UbPayloads}
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import org.apache.commons.codec.binary.Hex
import org.scalatest.{FeatureSpec, Matchers}

class MsgPackUbirch50
  extends FeatureSpec
    with Matchers
    with StrictLogging
    with MyJsonProtocol {

  val testMsgs = List(
    List("96cd0013b030aea423496812233445566778899aabda0040eaa430f241dd5f40fd055ac85842629c046482b99ae1ce5dd747864872ca9834b88847590b18300735b6598f5798d2ba18b9ada980a135747eb9c1b8705285083292cf000577f7638c438003da004029d701c401b35052521a4cb7ef5d5218dd49dfc2cb7dc9e69f2b32b46e22d16907c7005c2495cdd31969916138949d3dcfd08f6714d7c1a2aa86ff495882d901", """{"ts":"2018-10-11T17:34:54.317Z","a":3}"""),
    List("9613b0d3844deb370548f6a3059e2993fd3bfada0040bbe9c49ceac6639d2ac2852ff417bec1906d5492f9cda1fe144c1758385c743420574d95f1fabfb3e813011ef8042c6d04446b62f74739b4364dfbe8333c19073292cd04d2ce0006f855da00400b1316027b297699128835693915bae30c26e631818ba5bd6cbc0efd3f40396fbd5eaf07395f0f36371a29672fe8d4ffb39d5d7f1d003c6f4e0d9512f940c008", """{"ts":"1970-01-01T00:00:00.001Z","a":456789}""")
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
  }
}
