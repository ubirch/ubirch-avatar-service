package com.ubirch.avatar.model.rest.device

import org.joda.time.DateTime

import java.util.UUID

case class DeviceClaim(hwDeviceId: String, userId: UUID)

case class DeviceUserClaimRequest(
  hwDeviceId: String,
  userId: UUID
)

case class DeviceUserClaim(
  hwDeviceId: String,
  deviceId: String,
  userId: UUID,
  productionDate: DateTime
)
