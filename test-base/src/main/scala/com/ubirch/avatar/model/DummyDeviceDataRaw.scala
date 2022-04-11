package com.ubirch.avatar.model

import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.server.util.ServerKeys
import com.ubirch.util.crypto.hash.HashUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST.JValue
import org.json4s.native.JsonMethods._

import java.util.UUID
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
  : DeviceDataRaw = {

    val p = randomPayload(timestamp)
    val s = DeviceUtil.sign(p)

    DeviceDataRaw(
      id = messageId,
      did = Some(UUIDUtil.uuidStr),
      fw = "V1.2.3",
      a = HashUtil.sha512Base64(device.hwDeviceId),
      ts = timestamp,
      k = Some(ServerKeys.pubKeyB64),
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
      )

      rawDataList.+=:(deviceData)

    }

    (device, rawDataList.toList)

  }

  def randomPayload(ts: DateTime): JValue =
    parse(
      s"""
         |[
         |{
         |              "t" : 3648,
         |              "ts" : "2022-04-04T06:51:57.000Z"
         |            },
         |            {
         |              "t" : 3648,
         |              "ts" : "2022-04-04T06:52:57.000Z"
         |            },
         |            {
         |              "t" : 3649,
         |              "ts" : "2022-04-04T06:53:57.000Z"
         |            },
         |            {
         |              "t" : 3648,
         |              "ts" : "2022-04-04T06:54:57.000Z"
         |            },
         |            {
         |              "t" : 3650,
         |              "ts" : "2022-04-04T06:55:57.000Z"
         | }
         |]
        """.stripMargin
    )

}
