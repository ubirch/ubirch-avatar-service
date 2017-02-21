package com.ubirch.avatar.core.test.util

import com.ubirch.avatar.core.device.DeviceDataProcessedManager
import com.ubirch.avatar.model.DummyDeviceDataProcessed
import com.ubirch.avatar.model.device.DeviceDataProcessed

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
    * @param elementCount number of elements to create and store
    * @return list of stored messages (ordered by: timestamp ASC)
    */
  def storeSeries(elementCount: Int): Seq[DeviceDataProcessed] = {

    val dataSeries: Seq[DeviceDataProcessed] = DummyDeviceDataProcessed.dataSeries(elementCount = elementCount)
    store(dataSeries)

  }

  def store(list: Seq[DeviceDataProcessed]): Seq[DeviceDataProcessed] = {

    val storedSeries: ListBuffer[DeviceDataProcessed] = ListBuffer()

    list foreach { deviceData =>
      val storedRawData = Await.result(DeviceDataProcessedManager.store(deviceData), 1 seconds).get
      storedSeries += storedRawData
    }
    Thread.sleep(3000)

    storedSeries.toList

  }

}
