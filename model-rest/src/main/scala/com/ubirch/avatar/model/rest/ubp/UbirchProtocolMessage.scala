package com.ubirch.avatar.model.rest.ubp

import java.util.UUID

import org.json4s.JsonAST.JValue

/**
  *
  * @param version          version info of ubMesgpack message
  * @param mainVersion      main version info of ubMesgpack message
  * @param subVersion       sub version info of ubMesgpack message
  * @param hwDeviceId       hwDeviceId of device sending this data
  * @param hashedHwDeviceId hashed hwDeviceId
  * @param firmwareVersion  firmware deivce currentlz uses
  * @param prevSignature    array of bytes as String
  * @param msgType          ub protocol msg type
  * @param payloads         UbPayloads
  * @param signature        array of bytes as String
  * @param rawPayload       array of bytes as String
  * @param rawMessage       array of bytes as String
  * @param payloadHash      SHA512 hash of binary payload
  */
case class UbMessage(
                      version: Int,
                      mainVersion: Int,
                      subVersion: Int,
                      hwDeviceId: UUID,
                      hashedHwDeviceId: String,
                      firmwareVersion: Option[String] = None,
                      prevSignature: Option[String] = None,
                      msgType: Int = 0,
                      payloads: UbPayloads,
                      signature: Option[String] = None,
                      rawPayload: String,
                      rawMessage: String,
                      payloadHash: String
                    )

case class UbPayloads(
                       data: JValue,
                       meta: Option[JValue] = None,
                       config: Option[JValue] = None
                     )

case class UbTacklePayload(
                            version: String,
                            wakeups: Int,
                            status: Int,
                            temps: Map[String, Int]
                          )

abstract case class UbTrackleMessage(
                                      tracklePayload: UbTacklePayload
                                    )