package com.ubirch.avatar.model.db.device

import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}

/**
  * author: cvandrei
  * since: 2017-06-13
  *
  * @param deviceId          id of associated device, should be a UUID
  * @param inSync            means reported == desired
  * @param desired           JSON with changed device config and states from other sensors (actor -> sensor)
  * @param reported          (JSON) last state as reported by device
  * @param delta             (JSON) delta state = reported - desired
  * @param deviceLastUpdated timestamp of last reported state update
  * @param avatarLastUpdated timestamp of last desired state update
  */
case class AvatarState(deviceId: UUID,
                       inSync: Option[Boolean] = None,
                       desired: Option[String] = None,
                       reported: Option[String] = None,
                       delta: Option[String] = None,
                       // update on device
                       deviceLastUpdated: Option[DateTime] = Some(DateTime.now()),
                       // update on server side
                       avatarLastUpdated: Option[DateTime] = Some(DateTime.now())
                      )
