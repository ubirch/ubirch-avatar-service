package com.ubirch.avatar.model.device

import org.joda.time.DateTime
import org.json4s.JValue

/**
  * author: cvandrei
  * since: 2016-09-30
  * SimpleDeviceMessage should be used instead
  */
@deprecated
case class DeviceDataRaw(deviceId: String,
                         messageId: String,
                         deviceType: String,
                         timestamp: DateTime,
                         deviceTags: Seq[String],
                         deviceMessage: JValue
                     )
