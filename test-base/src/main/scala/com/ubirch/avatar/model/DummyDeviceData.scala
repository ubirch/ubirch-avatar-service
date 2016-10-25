package com.ubirch.avatar.model

import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST.JValue
import org.json4s.native.JsonMethods._

import scala.collection.mutable.ListBuffer

/**
  * author: cvandrei
  * since: 2016-10-25
  */
object DummyDeviceData {

  def data(deviceId: String = UUIDUtil.uuidStr,
           messageId: String = UUIDUtil.uuidStr,
           deviceType: String = "lightsLamp",
           timestamp: DateTime = DateTime.now,
           deviceTags: Seq[String] = Seq("ubirch#0", "actor"),
           deviceMessage: JValue = parse("""{"foo": 23, "bar": 42}""")
          ): DeviceData = {
    DeviceData(deviceId, messageId, deviceType, timestamp, deviceTags, deviceMessage)
  }

  def dataSeries(deviceId: String = UUIDUtil.uuidStr,
                 messageId: String = UUIDUtil.uuidStr,
                 deviceType: String = "lightsLamp",
                 deviceTags: Seq[String] = Seq("ubirch#0", "actor"),
                 deviceMessage: JValue = parse("""{"foo": 23, "bar": 42}"""),
                 intervalMillis: Long = 1000 * 10, // 10s
                 timestampOffset: Long = -1000 * 60 * 60, // 1h
                 elementCount: Int = 5
                ): List[DeviceData] = {

    val deviceDataList: ListBuffer[DeviceData] = ListBuffer()
    val newestDateTime = DateTime.now(DateTimeZone.UTC).minus(timestampOffset)

    val range = 0 until elementCount
    for (i <- range) {
      val timestamp = newestDateTime.minus(i * intervalMillis)
      val deviceData = data(deviceId, messageId, deviceType, timestamp, deviceTags, deviceMessage)
      deviceDataList.+=:(deviceData)
    }

    deviceDataList.toList

  }

}
