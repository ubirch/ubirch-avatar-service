package com.ubirch.avatar.model.rest.device

import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JValue

/**
  * author: cvandrei
  * since: 2017-06-16
  *
  * @param deviceId          id of associated device, should be a UUID
  * @param inSync            means reported == desired
  * @param desired           json with changed device config and states from other sensors (actor -> sensor)
  * @param reported          last state as reported by device
  * @param delta             delta state = reported - desired
  * @param deviceLastUpdated timestamp of last reported state update
  * @param avatarLastUpdated timestamp of last desired state update
  */
case class AvatarState(deviceId: String,
                       inSync: Option[Boolean] = None,
                       desired: Option[JValue] = None,
                       reported: Option[JValue] = None,
                       delta: Option[JValue] = None,
                       // update on device
                       deviceLastUpdated: Option[DateTime] = Some(DateTime.now(DateTimeZone.UTC)),
                       // update on server side
                       avatarLastUpdated: Option[DateTime] = Some(DateTime.now(DateTimeZone.UTC))
                      )
