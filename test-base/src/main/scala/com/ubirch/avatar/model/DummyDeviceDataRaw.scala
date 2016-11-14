package com.ubirch.avatar.model

import java.util.UUID

import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}
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
    DeviceDataRaw(id = messageId, a = device.hwDeviceId, k = Some(pubKey), ts = timestamp, s = hashedPubKey, p = payload())
  }

  def dataSeries(messageId: Option[UUID] = None,
                 device: Device = DummyDevices.minimalDevice(),
                 pubKey: String = "pretend-to-be-a-public-key",
                 intervalMillis: Long = 1000 * 10, // 10s
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
      val deviceData = data(messageId = msgId, device = device, pubKey = pubKey, timestamp = timestamp, hashedPubKey = hashedPubKey)(payload)
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
         |"h":${4000 + Random.nextInt(5500)}
         |}
         |]
        """.stripMargin
    )

}
