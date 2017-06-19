package com.ubirch.avatar.core.device

import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceInfo

/**
  * author: derMicha
  * since: 2016-11-07
  */
object DeviceStubManger {

  def toDeviceInfo(device: Device): DeviceInfo = {

    DeviceInfo(
      deviceId = device.deviceId,
      deviceName = device.deviceName,
      deviceTypeKey = device.deviceTypeKey,
      deviceLastUpdated = device.deviceLastUpdated,
      inSync = Some(true)
    )

  }

}
