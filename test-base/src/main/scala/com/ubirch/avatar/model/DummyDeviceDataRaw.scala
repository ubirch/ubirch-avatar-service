package com.ubirch.avatar.model

import java.util.UUID

import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JValue
import org.json4s.native.JsonMethods._

import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
  * author: cvandrei
  * since: 2016-10-25
  */
object DummyDeviceDataRaw {

  val random = new Random()

  def data(messageId: UUID = UUIDUtil.uuid,
           device: Device,
           pubKey: String = "pretend-to-be-a-public-key",
           timestamp: DateTime = DateTime.now,
           hashedPubKey: String = "pretend-to-be-a-public-key"
          )
          (payload: () => JValue = () => randomPayload())
  : DeviceDataRaw = {

    val p = payload()
    val (k, s) = DeviceUtil.sign(p, device)

    DeviceDataRaw(
      id = messageId,
      a = HashUtil.sha512Base64(device.hwDeviceId),
      ts = timestamp,
      k = Some(k),
      s = Some(s),
      p = p
    )

  }

  def dataSeries(messageId: Option[UUID] = None,
                 device: Device = DummyDevices.minimalDevice(),
                 pubKey: String = "pretend-to-be-a-public-key",
                 intervalMillis: Long = 1000 * 30, // 30s
                 timestampOffset: Long = -1000 * 60 * 60, // -1h
                 elementCount: Int = 5
                )
                (payload: () => JValue = () => randomPayload())
  : (Device, List[DeviceDataRaw]) = {

    val rawDataList: ListBuffer[DeviceDataRaw] = ListBuffer()
    val newestDateTime = DateTime.now(DateTimeZone.UTC).minus(timestampOffset)

    val hashedPubKey = pubKey match {
      case pk: String if pk.nonEmpty => HashUtil.sha256HexString(pk)
      case _ => "pretend-to-be-a-public-key"
    }

    val range = 0 until elementCount
    for (i <- range) {

      val timestamp = newestDateTime.minus(i * intervalMillis)
      val msgId = messageId match {
        case None => UUIDUtil.uuid
        case Some(m) => m
      }

      val deviceData = data(messageId = msgId,
        device = device,
        pubKey = pubKey,
        timestamp = timestamp,
        hashedPubKey = hashedPubKey
      )(payload)

      rawDataList.+=:(deviceData)

    }

    (device, rawDataList.toList)

  }

  def randomPayload(): JValue =
    parse(
      s"""
         |[
         |{
         |"t":${2000 + Random.nextInt(1500)},
         |"p":${90000 + Random.nextInt(20000)},
         |"h":${4000 + Random.nextInt(5500)},
         |"la":"52.51${10000 + Random.nextInt(20000)}",
         |"lo":"13.21${10000 + Random.nextInt(20000)}",
         |"a":${5000 + Random.nextInt(10000)}
         |}
         |]
        """.stripMargin
    )

}
