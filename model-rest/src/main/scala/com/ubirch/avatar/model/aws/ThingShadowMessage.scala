package com.ubirch.avatar.model.aws

import org.json4s.JValue

/**
  * Created by derMicha on 21/04/16.
  */
case class ThingShadowMessage(
                               state: ThingShadowState
                             )


case class ThingShadowState(
                             desired: Option[JValue] = None,
                             reported: Option[JValue] = None
                           )
