package com.ubirch.avatar.model.aws

import org.joda.time.DateTime
import org.json4s.JValue

/**
  * Created by derMicha on 21/04/16.
  */
case class ThingShadowMessage(
                               state: ThingShadowState
                             )


case class ThingShadowState(
                             syncState: Option[Boolean] = None,
                             desired: Option[JValue] = None,
                             reported: Option[JValue] = None,
                             delta: Option[JValue] = None,
                             deviceLastUpdated: Option[DateTime],
                             avatarLastUpdated: Option[DateTime]
                           )
