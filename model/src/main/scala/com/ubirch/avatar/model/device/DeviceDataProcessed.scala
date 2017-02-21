package com.ubirch.avatar.model.device

import java.util.UUID

import org.joda.time.DateTime
import org.json4s._

/**
  * author: cvandrei
  * since: 2016-11-02
  */
case class DeviceDataProcessed(deviceId: String, // TODO why is this a String and not a UUID?
                               messageId: UUID,
                               deviceDataRawId: UUID,
                               deviceType: String,
                               timestamp: DateTime,
                               deviceTags: Set[String],
                               deviceMessage: JValue,
                               deviceDataRaw: Option[DeviceDataRaw] = None
                              )
