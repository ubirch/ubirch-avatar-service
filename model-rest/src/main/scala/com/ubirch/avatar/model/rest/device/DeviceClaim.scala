package com.ubirch.avatar.model.rest.device

import java.util.UUID

case class DeviceClaim(hwDeviceId: String)

case class DeviceUserClaimRequest(
                                   hwDeviceId: String,
                                   externalId: String,
                                   userId: UUID
                                 )

case class DeviceUserClaim(
                            hwDeviceId: String,
                            deviceId: String,
                            userId: UUID
                          )
