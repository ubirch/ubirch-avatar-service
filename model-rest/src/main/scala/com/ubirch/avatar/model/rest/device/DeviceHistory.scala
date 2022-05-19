package com.ubirch.avatar.model.rest.device

import java.util.UUID

import org.joda.time.DateTime
import org.json4s._

/**
  * author: cvandrei
  * since: 2016-11-02
  */

case class DeviceHistory(
  messageId: UUID,
  deviceDataRawId: UUID,
  deviceId: String,
  deviceName: String,
  deviceType: String,
  deviceTags: Set[String],
  deviceMessage: JValue,
  deviceDataRaw: Option[DeviceDataRaw] = None,
  timestamp: DateTime)

case class DeviceHistoryLegacy(
  messageId: UUID,
  deviceDataRawId: UUID,
  deviceId: String,
  deviceName: Option[String] = None,
  deviceType: String,
  deviceTags: Set[String],
  deviceMessage: JValue,
  deviceDataRaw: Option[DeviceDataRaw] = None,
  timestamp: DateTime)
