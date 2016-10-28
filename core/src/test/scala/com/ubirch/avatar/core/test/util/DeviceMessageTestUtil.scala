package com.ubirch.avatar.core.test.util

import com.ubirch.avatar.core.device.DeviceMessageManager
import com.ubirch.avatar.model.DummyDeviceMessage
import com.ubirch.avatar.model.device.DeviceMessage

/**
  * author: cvandrei
  * since: 2016-10-27
  */
object DeviceMessageTestUtil {

  /**
    * Creates a series of device message and stores them. All messages are from the same device.
    *
    * @param elementCount number of elements to create and store
    * @return list of stored messages (ordered by: timestamp ASC)
    */
  def storeSeries(elementCount: Int): List[DeviceMessage] = {

    val dataSeries: List[DeviceMessage] = DummyDeviceMessage.dataSeries(elementCount = elementCount)

    dataSeries foreach { deviceData =>
      DeviceMessageManager.store(deviceData)
    }
    Thread.sleep(3000)

    dataSeries

  }

}
