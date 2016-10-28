package com.ubirch.avatar.model.device

import java.util.UUID

import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime
import org.json4s._

/**
  * Created by derMicha on 28/10/16.
  *
  * @param messageId                unique message id
  * @param simpleDeviceMessageRefId refers to original SimpleDeviceMessage
  * @param version                  verison Id which identifies the version of the message type
  * @param deviceId                 id of the device which sends the message
  * @param payload                  data as JSON / JValue
  * @param timestamp                timestamp of the original SimpleDeviceMessage
  */
case class TransformedDeviceMessage(
                                     messageId: UUID = UUIDUtil.uuid,
                                     simpleDeviceMessageRefId: UUID,
                                     version: String = "0.0.3",
                                     deviceId: String,
                                     payload: JValue,
                                     timestamp: Option[DateTime] = None
                                   )
