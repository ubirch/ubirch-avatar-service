package com.ubirch.avatar.cmd

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.protocol.ProtocolMessage
import com.ubirch.protocol.codec.{ MsgPackProtocolDecoder, MsgPackProtocolEncoder, ProtocolHints }
import com.ubirch.util.crypto.hash.HashUtil
import org.apache.commons.codec.binary.Hex
import sttp.client3.asynchttpclient.future.AsyncHttpClientFutureBackend
import sttp.client3.{ basicRequest, ignore, SttpBackend, UriContext }
import sttp.model.StatusCode

import java.nio.charset.StandardCharsets
import java.util.{ Base64, UUID }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, Future }

object TrackleProxy extends App with StrictLogging {

  /**
    * 1) unpack trackle msgPack (and validate?)
    * 2) removes payload
    * 3) add hash as payload
    * 4) change UPP hint
    * 5) update UPP version
    * 6) send to Niomon (with keyId and pw?)
    * 7) check response unknown msgPack and later 200
    */

  val backend: SttpBackend[Future, Any] = AsyncHttpClientFutureBackend()
  private val NIOMON_URL = "https://niomon.dev.ubirch.com/"
  val HARDWARE_ID_HEADER_KEY = "x-ubirch-hardware-id"
  val UBIRCH_AUTH_TYPE_KEY = "x-ubirch-auth-type"
  val UBIRCH_AUTH_TYPE_VALUE = "ubirch"
  val X_UBIRCH_CREDENTIAL_KEY = "x-ubirch-credential"
  val CREDENTIAL_VALUE =
    Base64.getEncoder.encodeToString("0e53ad9b-8a20-402b-9919-5c820c47e1bb".getBytes(StandardCharsets.UTF_8))
  val uuid: UUID = UUID.fromString("d3407cca-cbfa-474d-8d57-433643eb1e58")

  private val validHexData =
    "9613b0d3407ccacbfa474d8d57433643eb1e58da0040000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005495da002376312e302e322d50524f442d3230313830333236313033323035202876352e362e3629050385ce62e28617cd0deece62e28653cd0f9ece62e2868fcd0f8ece62e286cbcd0f8ace62e28707cd0ff184a36d696ecd0daca36d6178cd1068a169cdea60a2696cce001b7740da00405521910927485e8cc201cf15fb89db4949e0b9a6d4ac9c362ca60ba90b6579b00a24b30f3e17d8d266aeb509b7f3246f8da985667819c8f5d110f679768bf104"
  private val oldTrackleUPP = Hex.decodeHex(validHexData.toCharArray)

  val pm = MsgPackProtocolDecoder.getDecoder.decode(oldTrackleUPP)
  val dataAndSignature = MsgPackProtocolDecoder.getDecoder.getDataToVerifyAndSignature(oldTrackleUPP)
  val hashedData = HashUtil.sha512(dataAndSignature(0))

  val newPM = new ProtocolMessage(
    ProtocolMessage.CHAINED,
    pm.getUUID,
    pm.getChain,
    ProtocolHints.HASHED_TRACKLE_MSG_PACK_HINT,
    hashedData)
  newPM.setSignature(pm.getSignature)
  newPM.setSigned("dummy".getBytes())
  val encoded = MsgPackProtocolEncoder.getEncoder.encode(newPM)

  val dataToVerifyAndSignature =
    MsgPackProtocolDecoder.getDecoder.getDataToVerifyAndSignature(encoded)

  println("signature: " + Base64.getEncoder.encodeToString(pm.getSignature))
  println("signed / SHA-512 hash: " + Base64.getEncoder.encodeToString(hashedData))
  println("new hashed trackle msg: " + Hex.encodeHexString(encoded))
  println("dataToVerify: " + Hex.encodeHexString(dataToVerifyAndSignature(0)))
  sendHashedTrackleMsgToNiomon(encoded)
  def sendHashedTrackleMsgToNiomon(encoded: Array[Byte]): Boolean = {
    Await.result(
      basicRequest
        .post(uri"$NIOMON_URL")
        .body(encoded)
        .header(UBIRCH_AUTH_TYPE_KEY, UBIRCH_AUTH_TYPE_VALUE)
        .header(X_UBIRCH_CREDENTIAL_KEY, CREDENTIAL_VALUE)
        .header(HARDWARE_ID_HEADER_KEY, uuid.toString)
        .send(backend).map {
          _.code match {
            case StatusCode.Ok =>
              print("Send successfully")
              true
            case code =>
              println(s"Problem status code $code")
              false
          }
        },
      10.seconds
    )
  }

}
