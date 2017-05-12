package com.ubirch.avatar.model.rest.payload

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
                                       e: Int
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
                                          e: Int
                                        )
