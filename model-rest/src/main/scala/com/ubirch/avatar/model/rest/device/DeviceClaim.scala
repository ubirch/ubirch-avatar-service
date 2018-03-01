package com.ubirch.avatar.model.rest.device

import java.util.UUID

case class DeviceClaim(hwDeviceId: String)

case class DeviceUserClaim(hwDeviceId: String, userId: String)
