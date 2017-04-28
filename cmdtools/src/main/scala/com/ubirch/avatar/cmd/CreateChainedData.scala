package com.ubirch.avatar.cmd

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.avatar.model.{DummyDeviceDataRaw, DummyDevices}
import com.ubirch.keyservice.KeyServiceManager
import com.ubirch.util.json.Json4sUtil

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by derMicha on 24/04/17.
  */
object CreateChainedData
  extends App
    with StrictLogging {

  val elementCount = 5
  val device = DummyDevices.minimalDevice()
  val dataSeries: List[DeviceDataRaw] = DummyDeviceDataRaw.dataSeries(device = device, elementCount = elementCount)()

  KeyServiceManager.getKeyPairForDevice(device.deviceId).map {
    case Some(keyPair) =>
      println("public Key: " + KeyServiceManager.encodePubKey(keyPair.getPublic))
      println("private Key: " + KeyServiceManager.encodePrivateKey(keyPair.getPrivate))
    case None => None
  }

  var prevDataHash: Option[String] = None

  val chainedDataSeries: List[DeviceDataRaw] = dataSeries.map { ddr =>
    val chainedDdr = ddr.copy(ch = prevDataHash, k = None)
    prevDataHash = Some(DeviceDataRawManager.payloadHash(chainedDdr))
    chainedDdr
  }
  chainedDataSeries.foreach { ddr =>
    val jval = Json4sUtil.any2jvalue(ddr).get

    KeyServiceManager.getKeyPairForDevice(device.deviceId).map {
      case Some(keyPair) =>
        println(Json4sUtil.jvalue2String(jval))
      case None => None
    }
  }


  logger.info(s"created: ${dataSeries.size}")
}
