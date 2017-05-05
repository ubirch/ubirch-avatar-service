package com.ubirch.avatar.test.tools.model

import java.util.UUID

import com.ubirch.avatar.model.device.DeviceHistory
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST.JValue
import org.json4s.native.JsonMethods._

import scala.collection.mutable.ListBuffer

/**
  * author: cvandrei
  * since: 2016-10-25
  */
object DummyDeviceHistory {

  def data(deviceId: String = UUIDUtil.uuidStr,
           messageId: UUID = UUIDUtil.uuid,
           deviceType: String = "lightsLamp",
           timestamp: DateTime = DateTime.now,
           deviceTags: Set[String] = Set("ubirch#0", "actor"),
           deviceMessage: JValue = parse("""{"foo": 23, "bar": 42}""")
          ): DeviceHistory = {

    DeviceHistory(
      messageId = messageId,
      deviceDataRawId = UUIDUtil.uuid,
      deviceId = deviceId,
      deviceName = s"$deviceType $deviceId",
      deviceType = deviceType,
      deviceTags = deviceTags,
      deviceMessage = deviceMessage,
      timestamp = timestamp
    )
  }

  def dataSeries(deviceId: String = UUIDUtil.uuidStr,
                 dType: String = "lightsLamp",
                 tags: Set[String] = Set("ubirch#0", "actor"),
                 message: JValue = parse("""{"foo": 23, "bar": 42}"""),
                 intervalMillis: Long = 1000 * 10, // 10s
                 timestampOffset: Long = -1000 * 60 * 60, // 1h
                 elementCount: Int = 5
                ): List[DeviceHistory] = {

    val deviceDataList: ListBuffer[DeviceHistory] = ListBuffer()
    val newestDateTime = DateTime.now(DateTimeZone.UTC).minus(timestampOffset)

    val range = 0 until elementCount
    for (i <- range) {
      val timestamp = newestDateTime.minus(i * intervalMillis)
      val deviceData = data(deviceId = deviceId, deviceType = dType, timestamp = timestamp, deviceTags = tags, deviceMessage = message)
      deviceDataList.+=:(deviceData)
    }

    deviceDataList.toList

  }

}
