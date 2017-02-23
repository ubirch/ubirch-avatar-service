package com.ubirch.avatar.model.device

import java.util.UUID

import com.ubirch.avatar.model.MessageVersion
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.DateTime
import org.json4s._

/**
  * Created by derMicha on 28/10/16.
  *
  * @param version                  verison Id which identifies the version of the message type
  * @param messageId                unique message id
  * @param deviceId                 id of the device which sends the message
  * @param simpleDeviceMessageRefId refers to original DeviceDataRaw
  * @param error                    device error message
  * @param config                   device config
  * @param payload                  data as JSON / JValue
  * @param timestamp                timestamp of the original DeviceDataRaw
  */
case class TransformedDeviceMessage(
                                     version: String = MessageVersion.v003,
                                     messageId: UUID = UUIDUtil.uuid,
                                     deviceId: String,
                                     validationState: Option[String],
                                     signature: Option[String],
                                     simpleDeviceMessageRefId: UUID,
                                     error: Option[String],
                                     config: JValue,
                                     payload: JValue,
                                     timestamp: Option[DateTime] = None
                                   )
