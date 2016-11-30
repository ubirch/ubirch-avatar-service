package com.ubirch.avatar.model.device

/**
  * Created by derMicha on 29/11/16.
  */
final case class EnvSensorPayload(
                                   temperature: Double,
                                   presure: Double,
                                   humidity: Double,
                                   batteryLevel: Int,
                                   latitude: Double,
                                   longitude: Double,
                                   loops: Long,
                                   altitude: Double,
                                   errorCode: Int
                                 )

final case class EnvSensorRawPayload(
                                      t: Int,
                                      p: Int,
                                      h: Int,
                                      ba: Int,
                                      la: String,
                                      lo: String,
                                      lp: Long,
                                      a: Double,
                                      e: Int
                                    )
