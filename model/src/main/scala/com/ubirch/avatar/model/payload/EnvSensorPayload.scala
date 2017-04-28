package com.ubirch.avatar.model.payload

/**
  * Created by derMicha on 29/11/16.
  */
final case class EnvSensorPayload(
                                   temperature: Double,
                                   presure: Double,
                                   humidity: Double,
                                   batteryLevel: Option[Int],
                                   latitude: Option[Double],
                                   longitude: Option[Double],
                                   loops: Option[Long],
                                   altitude: Option[Double],
                                   errorCode: Option[Int]
                                 )

final case class AqSensorPayload(
                                  airquality: Int,
                                  airqualityRef: Int,
                                  temperature: Double,
                                  presure: Double,
                                  humidity: Double,
                                  batteryLevel: Option[Int],
                                  latitude: Option[Double],
                                  longitude: Option[Double],
                                  loops: Option[Long],
                                  altitude: Option[Double],
                                  errorCode: Option[Int]
                                )

final case class EnvSensorRawPayload(
                                      t: Int,
                                      p: Int,
                                      h: Int,
                                      ba: Option[Int],
                                      la: Option[String],
                                      lo: Option[String],
                                      lp: Option[Long],
                                      a: Option[Double],
                                      e: Option[Int]
                                    )

final case class AqSensorRawPayload(
                                     aq: Int,
                                     aqr: Int,
                                     t: Int,
                                     p: Int,
                                     h: Int,
                                     ba: Option[Int],
                                     la: Option[String],
                                     lo: Option[String],
                                     lp: Option[Long],
                                     a: Option[Double],
                                     e: Option[Int]
                                   )
