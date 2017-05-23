package com.ubirch.avatar.model.rest.payload

import org.joda.time.DateTime

/**
  * Created by derMicha on 01/02/17.
  */

final case class TrackleSensorPayload(
                                       ts: Int,
                                       ba: Int,
                                       pc: Int,
                                       t1: Int,
                                       t2: Int,
                                       t3: Int,
                                       la: String,
                                       lo: String,
                                       e: Int,
                                       dt: Option[DateTime] = None
                                     )

final case class TrackleSensorPayloadOut(
                                          ts: Int,
                                          ba: Int,
                                          pc: Int,
                                          t1Adc: Int,
                                          t2Adc: Int,
                                          t3Adc: Int,
                                          t1: Double,
                                          t2: Double,
                                          t3: Double,
                                          la: String,
                                          lo: String,
                                          e: Int,
                                          dt: Option[DateTime] = None
                                        )
