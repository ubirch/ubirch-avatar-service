package com.ubirch.avatar.model.rest.device

/**
  * Created by derMicha on 07/11/16.
  */
case class DeviceShortInfo(
                            deviceId: String,
                            deviceName: String,
                            deviceTypeKey: String = "unknownDeviceType"
                          )