package com.ubirch.avatar.model.db.device

import org.joda.time.DateTime

/**
  * author: cvandrei
  * since: 2017-06-13
  *
  * @param deviceId               UUID of related device
  * @param desired                JSON with changed device config and states from other sensors (actor -> sensor)
  * @param reported               (JSON) most recent state reported by device
  * @param currentDeviceSignature signature from the current incoming device message
  * @param deviceLastUpdated      most recent update of _reported_
  * @param avatarLastUpdated      most recent update of _desired_
  */
case class AvatarState(deviceId: String,
                       desired: Option[String] = Some("{}"),
                       reported: Option[String] = Some("{}"),
                       currentDeviceSignature: Option[String] = None,
                       deviceLastUpdated: Option[DateTime] = Some(DateTime.now()),
                       avatarLastUpdated: Option[DateTime] = Some(DateTime.now())
                      )
