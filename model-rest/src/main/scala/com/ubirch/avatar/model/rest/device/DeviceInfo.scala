package com.ubirch.avatar.model.rest.device

import org.joda.time.DateTime

/**
  * Created by derMicha on 07/11/16.
  */
final case class DeviceInfo(
                             deviceId: String,
                             hwDeviceId: String,
                             deviceName: String,
                             deviceTypeKey: String = "unknownDeviceType",
                             deviceLastUpdated: Option[DateTime],
                             inSync: Option[Boolean]
                           )