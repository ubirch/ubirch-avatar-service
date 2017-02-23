package com.ubirch.avatar.core.test.util

import java.util.UUID

import com.ubirch.avatar.core.device.DeviceDataProcessedManager
import com.ubirch.avatar.model.DummyDeviceDataProcessed
import com.ubirch.avatar.model.device.DeviceDataProcessed
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-10-27
  */
object DeviceDataProcessedTestUtil {

  /**
    * Creates a series of [[DeviceDataProcessed]] and stores them. All messages are from the same device.
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
                 ): Seq[DeviceDataProcessed] = {

    val now = DateTime.now(DateTimeZone.UTC)
    val midnight = now.withHourOfDay(23)
      .withMinuteOfHour(59)
      .withSecondOfMinute(59)
      .withMillisOfSecond(0)

    val dataSeries: Seq[DeviceDataProcessed] = DummyDeviceDataProcessed.dataSeries(
      deviceId = deviceId,
      elementCount = elementCount,
      intervalMillis = 1000 * 5, // 5s
      timestampOffset = -1 * (midnight.getMillis - now.getMillis)
    )
    store(dataSeries)

  }

  /**
    * Create a list of [[DeviceDataProcessed]] instances based on the given list of timestamps and store them.
    *
    * @param deviceId   deviceId for all instances
    * @param timestamps create [[DeviceDataProcessed]] for each timestamp
    * @return list of stored instances
    */
  def storeTimeBasedSeries(deviceId: UUID, timestamps: Seq[DateTime]): Seq[DeviceDataProcessed] = {

    val toStore: Seq[DeviceDataProcessed] = timestamps map { t =>
      DummyDeviceDataProcessed.data(deviceId = deviceId.toString, timestamp = t)
    }

    DeviceDataProcessedTestUtil.store(toStore)

  }

  /**
    * Store a list of [[DeviceDataProcessed]] instances.
    *
    * @param list instances to store
    * @return list of stored instances
    */
  def store(list: Seq[DeviceDataProcessed]): Seq[DeviceDataProcessed] = {

    val storedSeries: ListBuffer[DeviceDataProcessed] = ListBuffer()

    list foreach { deviceData =>
      val storedRawData = Await.result(DeviceDataProcessedManager.store(deviceData), 2 seconds).get
      storedSeries += storedRawData
    }
    Thread.sleep(1500 + list.size)

    storedSeries.toList

  }

}
