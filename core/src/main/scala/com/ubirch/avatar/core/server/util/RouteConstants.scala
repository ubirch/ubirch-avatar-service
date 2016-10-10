package com.ubirch.avatar.core.server.util

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object RouteConstants {

  val apiPrefix = "api"
  val currentVersion = "v1"
  val serviceName = "avatarService"
  val device = "device"
  val stub = "stub"

  val urlPrefix = s"/$apiPrefix/$serviceName/$currentVersion"

  val urlDevice = s"$urlPrefix/$device"

  val urlDeviceWithIdPrefix = s"$urlPrefix/$device"

  def urlDeviceWithId(id: String): String = s"$urlDeviceWithIdPrefix/$id"

  val urlDeviceStubWithIdPrefix = s"$urlPrefix/$device/$stub"
  def urlDeviceStubWithId(id: String): String = s"$urlDeviceStubWithIdPrefix/$id"

}
