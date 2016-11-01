package com.ubirch.avatar.core.test.util

import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.model.DummyDeviceDataRaw
import com.ubirch.avatar.model.device.DeviceDataRaw

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-10-27
  */
object DeviceDataRawTestUtil {

  /**
    * Creates a series of device message and stores them. All messages are from the same device.
    *
    * @param elementCount number of elements to create and store
    * @return list of stored messages (ordered by: timestamp ASC)
    */
  def storeSeries(elementCount: Int): List[DeviceDataRaw] = {

    val dataSeries: List[DeviceDataRaw] = DummyDeviceDataRaw.dataSeries(elementCount = elementCount)
    val storedSeries: ListBuffer[DeviceDataRaw] = ListBuffer()

    dataSeries foreach { deviceData =>
      val storedRawData = Await.result(DeviceDataRawManager.store(deviceData), 1 seconds).get
      storedSeries += storedRawData
    }
    Thread.sleep(3000)

    storedSeries.toList

  }

}
