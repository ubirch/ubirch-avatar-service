package com.ubirch.avatar.core.device

import com.ubirch.avatar.model.rest.device.{Device, DeviceInfo}

/**
  * Created by derMicha on 07/11/16.
  */
object DeviceStubManger {

  def create(device: Device): DeviceInfo = {

    //@TODO AWSIOT removed
    //    val awsSyncState = AwsShadowService.getSyncState(device.awsDeviceThingId)

    DeviceInfo(
      deviceId = device.deviceId,
      deviceName = device.deviceName,
      deviceTypeKey = device.deviceTypeKey,
      deviceLastUpdated = device.deviceLastUpdated,
      //      inSync = awsSyncState
      inSync = Some(true)
    )
  }
}
