package com.ubirch.avatar.model.rest.device

import org.joda.time.DateTime
import org.json4s.JValue

/**
  * we might use this draft in the future
  */
case class AvatarDraft(id: String,
                       ºstateDesired: Option[JValue],
                       stateReported: Option[JValue],
                       created: Option[DateTime],
                       updated: Option[DateTime],
                       lastActive: Option[DateTime]
                      )