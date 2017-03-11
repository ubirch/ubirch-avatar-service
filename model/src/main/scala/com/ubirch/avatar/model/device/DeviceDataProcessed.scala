package com.ubirch.avatar.model.device

import java.util.UUID

import org.joda.time.DateTime
import org.json4s._

/**
  * author: cvandrei
  * since: 2016-11-02
  */
case class DeviceDataProcessed(messageId: UUID,
                               deviceDataRawId: UUID,
                               deviceId: String, // TODO why is this a String and not a UUID? --> Device.deviceId is a String, too
                               deviceName: String,
                               deviceType: String,
                               deviceTags: Set[String],
                               deviceMessage: JValue,
                               deviceDataRaw: Option[DeviceDataRaw] = None,
                               timestamp: DateTime
                              )
