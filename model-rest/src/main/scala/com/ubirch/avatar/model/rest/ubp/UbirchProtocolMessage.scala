package com.ubirch.avatar.model.rest.ubp

import java.util.UUID

import org.json4s.JsonAST.JValue

/**
  *
  * @param version
  * @param mainVersion
  * @param subVersion
  * @param hwDeviceId
  * @param hashedHwDeviceId
  * @param firmwareVersion
  * @param prevSignature array of bytes as String
  * @param msgType
  * @param payloads      UbPayloads
  * @param signature     array of bytes as String
  * @param rawPayload    array of bytes as String
  * @param rawMessage    array of bytes as String
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
                      rawMessage: String
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