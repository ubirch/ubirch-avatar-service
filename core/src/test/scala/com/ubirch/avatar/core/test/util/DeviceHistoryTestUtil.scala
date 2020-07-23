package com.ubirch.avatar.core.test.util

import java.util.UUID

import com.ubirch.avatar.core.device.DeviceHistoryManager
import com.ubirch.avatar.core.test.model.DummyDeviceHistory
import com.ubirch.avatar.model.rest.device.DeviceHistory
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.mutable.ListBuffer
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-10-27
  */
object DeviceHistoryTestUtil {

  /**
    * Creates a series of [[DeviceHistory]] and stores them. All messages are from the same device.
    *
    * The newest message's timestamp is about 23:59:59.000 and the interval between messages is small enough to fit more
    * than 15.000 messages into one day. As an effect unless you create too many records all of their timestamps will be
    * on the same day.
    *
    * @param elementCount number of elements to create and store
    * @return list of stored messages (ordered by: timestamp ASC)
    */
  def storeSeries(elementCount: Int,
                  deviceId: String = UUIDUtil.uuidStr
                 ): Seq[DeviceHistory] = {

    val now = DateTime.now(DateTimeZone.UTC)
    val midnight = now.withHourOfDay(23)
      .withMinuteOfHour(59)
      .withSecondOfMinute(59)
      .withMillisOfSecond(0)

    val dataSeries: Seq[DeviceHistory] = DummyDeviceHistory.dataSeries(
      deviceId = deviceId,
      elementCount = elementCount,
      intervalMillis = 1000 * 5, // 5s
      timestampOffset = -1 * (midnight.getMillis - now.getMillis)
    )
    store(dataSeries)

  }

  /**
    * Create a list of [[DeviceHistory]] instances based on the given list of timestamps and store them.
    *
    * @param deviceId   deviceId for all instances
    * @param timestamps create [[DeviceHistory]] for each timestamp
    * @return list of stored instances
    */
  def storeTimeBasedSeries(deviceId: UUID, timestamps: Seq[DateTime]): Seq[DeviceHistory] = {

    val toStore: Seq[DeviceHistory] = timestamps map { t =>
      DummyDeviceHistory.data(deviceId = deviceId.toString, timestamp = t)
    }

    DeviceHistoryTestUtil.store(toStore)

  }

  /**
    * Store a list of [[DeviceHistory]] instances.
    *
    * @param list instances to store
    * @return list of stored instances
    */
  def store(list: Seq[DeviceHistory]): Seq[DeviceHistory] = {

    val storedSeries: ListBuffer[DeviceHistory] = ListBuffer()

    list foreach { deviceData =>
      val storedRawData = DeviceHistoryManager.store(deviceData).get
      storedSeries += storedRawData
    }
    Thread.sleep(2000 + list.size)

    storedSeries.toList

  }

}
