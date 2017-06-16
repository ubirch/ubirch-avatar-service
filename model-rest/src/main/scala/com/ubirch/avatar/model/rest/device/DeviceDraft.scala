package com.ubirch.avatar.model.rest.device

import org.joda.time.DateTime
import org.json4s.JValue

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
