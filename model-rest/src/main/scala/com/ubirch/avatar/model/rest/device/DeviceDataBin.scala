package com.ubirch.avatar.model.rest.device

import java.util.UUID

import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime

case class DeviceDataBin(
  id: UUID = UUIDUtil.uuid, // messageId
  deviceId: String,
  data: String,
  chainHash: String,
  signature: String,
  created: DateTime,
  updated: DateTime
)
