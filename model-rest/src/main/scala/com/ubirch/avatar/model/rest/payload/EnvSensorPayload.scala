package com.ubirch.avatar.model.rest.payload

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

final case class EmoSensorPayload(
                                   temperature: Double,
                                   emg: Int,
                                   gsr: Int,
                                   pulse: Int,
                                   activity: Int,
                                   emoDeviceId: Int,
                                   messageId: Int,
                                   batteryLevel: Option[Int] = None,
                                   errorCode: Option[Int] = None
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

final case class EmoSensorRawPayload(
                                      tmp: Int,
                                      emg: Int,
                                      gsr: Int,
                                      pls: Int,
                                      act: Int,
                                      did: Int,
                                      mid: Int,
                                      bat: Option[Int] = None,
                                      e: Option[Int]
                                    )
