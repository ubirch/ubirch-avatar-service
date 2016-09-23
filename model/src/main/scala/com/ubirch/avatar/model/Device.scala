package com.ubirch.avatar.model

import org.joda.time.DateTime
import org.json4s.JValue

/**
  * author: cvandrei
  * since: 2016-09-23
  */

case class Device(deviceId: String,
                  deviceType: Option[String],
                  deviceName: Option[String],
                  hwDeviceId: Option[String],
                  syncState: Option[String],
                  tags: Option[Seq[String]],
                  deviceConfig: Option[JValue],
                  deviceProperties: Option[JValue],
                  subscriptions: Option[Seq[String]],
                  avatarState: Option[AvatarState],
                  avatarLastUpdated: Option[DateTime],
                  created: Option[DateTime],
                  updated: Option[DateTime],
                  deviceLastUpdated: Option[DateTime]
                 )

case class AvatarState(desired: Option[JValue],
                       reported: Option[JValue]
                      )

/**
  * we might use this draft in the future
  */
case class DeviceDraft(id: String,
                       name: Option[String],
                       hwType: Option[String],
                       hwId: Option[String],
                       syncState: Option[Int], // 0 = out of sync, 1 = in sync, 100 = unknown
                       tags: Option[Set[String]],
                       properties: Option[JValue],
                       subscriptions: Option[Set[String]],
                       config: Option[JValue],
                       avatar: Option[AvatarDraft],
                       created: Option[DateTime],
                       updated: Option[DateTime],
                       lastActive: Option[DateTime]
                      )

/**
  * we might use this draft in the future
  */
case class AvatarDraft(id: String,
                       stateDesired: Option[JValue],
                       stateReported: Option[JValue],
                       created: Option[DateTime],
                       updated: Option[DateTime],
                       lastActive: Option[DateTime]
                      )