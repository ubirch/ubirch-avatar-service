package com.ubirch.avatar.core.test.util

import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.model.{DummyDeviceDataRaw, DummyDevices}
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

import scala.concurrent.ExecutionContext.Implicits.global

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
  def storeSeries(elementCount: Int): (Device, List[DeviceDataRaw]) = {

    val device = DummyDevices.minimalDevice()
    val dataSeries: List[DeviceDataRaw] = DummyDeviceDataRaw.dataSeries(device = device, elementCount = elementCount)()

    val storedSeries = dataSeries map { deviceData =>
      Await.result(DeviceDataRawManager.store(deviceData), 1 seconds).get
    }
    Thread.sleep(3000)

    (device, storedSeries)

  }

}
