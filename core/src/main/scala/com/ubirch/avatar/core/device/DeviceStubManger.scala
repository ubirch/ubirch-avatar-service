package com.ubirch.avatar.core.device

import com.ubirch.avatar.awsiot.services.AwsShadowService
import com.ubirch.avatar.model.device.{Device, DeviceStub}

/**
  * Created by derMicha on 07/11/16.
  */
object DeviceStubManger {

  def create(device: Device): DeviceStub = {

    val awsSyncState = AwsShadowService.getSyncState(device.awsDeviceThingId)

    DeviceStub(
      deviceId = device.deviceId,
      deviceName = device.deviceName,
      deviceTypeKey = device.deviceTypeKey,
      deviceLastUpdated = device.deviceLastUpdated,
      inSync = awsSyncState
    )
  }
}
