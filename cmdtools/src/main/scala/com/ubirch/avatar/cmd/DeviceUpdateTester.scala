package com.ubirch.avatar.cmd

import java.net.URL
import java.util.Base64

import com.ubirch.avatar.model.rest.MessageVersion
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.crypto.ecc.EccUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST.JArray
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import uk.co.bigbeeconsultants.http.HttpClient
import uk.co.bigbeeconsultants.http.header.{Header, Headers, MediaType}
import uk.co.bigbeeconsultants.http.request.RequestBody

import scala.util.Random

object DeviceUpdateTester extends App with MyJsonProtocol {

  val avsApiUrl = new URL("http://localhost:8080/api/avatarService/v1/device/update")

  val (pubKey, privKey) = EccUtil.generateEccKeyPair

  val pubKey64 = Base64.getEncoder.encodeToString(pubKey.getEncoded)
  val privKey64 = Base64.getEncoder.encodeToString(privKey.getEncoded)

  val hashedDeviceId = "7u5KLfooUMnQrK2UHHTfrhuZwdhqcBafPUYivMNkJXLeggaAcsaval+CvfelNbzoRnfPpGGUhS4krs2ddHO9rg=="

  val payload = JArray(List(
    ("r" -> Random.nextInt(255)) ~
      ("g" -> Random.nextInt(255)) ~
      ("b" -> Random.nextInt(255)) ~
      ("ts" -> DateTime.now(DateTimeZone.UTC).minusSeconds(1).toDateTimeISO.toString),
    ("r" -> Random.nextInt(255)) ~
      ("g" -> Random.nextInt(255)) ~
      ("b" -> Random.nextInt(255)) ~
      ("ts" -> DateTime.now(DateTimeZone.UTC).toDateTimeISO.toString)
  ))

  val payloadJson = render(payload)
  val payloadStr = Json4sUtil.jvalue2String(payload)
  println(payloadStr)

  val signature = EccUtil.signPayload(privKey64, payloadStr)

  val ddr = DeviceDataRaw(
    v = MessageVersion.v003,
    a = hashedDeviceId,
    k = Some(pubKey64),
    ts = DateTime.now(DateTimeZone.UTC).toDateTimeISO,
    p = payload,
    s = Some(signature)
  )

  val ddrStr = Json4sUtil.jvalue2String(Json4sUtil.any2jvalue(ddr).get)

  println(ddrStr)

  val hc = new HttpClient()
  val headers = Headers(List[Header](Header("Content-Type:application/json")))
  val body = RequestBody(contentType = MediaType.APPLICATION_JSON, string = ddrStr)

  doPost
  Thread.sleep(500)
  doPost
  Thread.sleep(2000)
  doPost

  //  DeviceDataRawManager.loadBySignature("zWH2v0fTYj5rkIOg2hHxqkKEBMSusjN11p0uVR2yTGa4bKL880AXsTOPMWnIp1sPUFonRzLqkfrCzsMKtMoBBA==").onComplete {

  //  println("Test1")
  //  DeviceDataRawManager.history(hashedDeviceId, 0, 10).map {res =>
  //    println(res.size)
  //  }
  //  println("Test2")
  //  DeviceDataRawManager.loadById(UUID.fromString("b3687b79-3c4b-419f-a740-7043096d4430")).onComplete {
  //    case Success(f) =>
  //      f match {
  //        case Some(d) =>
  //          println(d.deviceName)
  //        case None =>
  //          println("määääähh")
  //      }
  //    case Failure(t) =>
  //      t.printStackTrace()
  //  }

  println("Ende")

  private def doPost = {
    val resp = hc.post(url = avsApiUrl, body = Some(body), requestHeaders = headers)
    println(resp.body.asString)
  }
}
