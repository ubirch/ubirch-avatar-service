package com.ubirch.avatar.model

import java.util.UUID

import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}
import com.ubirch.util.crypto.hash.HashUtil
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST.JValue
import org.json4s.native.JsonMethods._

import scala.collection.mutable.ListBuffer

/**
  * author: cvandrei
  * since: 2016-10-25
  */
object DummyDeviceDataRaw {

  def data(messageId: UUID = UUIDUtil.uuid,
           device: Device,
           pubKey: Option[String] = None,
           timestamp: Option[DateTime] = Some(DateTime.now),
           hashedPubKey: String = "pretend-to-be-a-public-key",
           payload: JValue = parse("""{"foo": 23, "bar": 42}""")
          ): DeviceDataRaw = {
    DeviceDataRaw(id = messageId, a = device.hwDeviceId, k = pubKey, ts = timestamp, s = hashedPubKey, p = payload)
  }

  def dataSeries(messageId: UUID = UUIDUtil.uuid,
                 device: Device = DummyDevices.minimalDevice(),
                 pubKey: Option[String] = None,
                 payload: JValue = parse("""{"foo": 23, "bar": 42}"""),
                 intervalMillis: Long = 1000 * 10, // 10s
                 timestampOffset: Long = -1000 * 60 * 60, // 1h
                 elementCount: Int = 5
                ): (Device, List[DeviceDataRaw]) = {

    val rawDataList: ListBuffer[DeviceDataRaw] = ListBuffer()
    val newestDateTime = DateTime.now(DateTimeZone.UTC).minus(timestampOffset)

    val hashedPubKey = pubKey match {
      case None => "pretend-to-be-a-public-key"
      case Some(pk) => HashUtil.sha256HexString(pk)
    }

    val range = 0 until elementCount
    for (i <- range) {
      val timestamp = Some(newestDateTime.minus(i * intervalMillis))
      val deviceData = data(messageId = messageId, device = device, pubKey = pubKey, timestamp = timestamp, hashedPubKey = hashedPubKey, payload = payload)
      rawDataList.+=:(deviceData)
    }

    (device, rawDataList.toList)

  }

}
