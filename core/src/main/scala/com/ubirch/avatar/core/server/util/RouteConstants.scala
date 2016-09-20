package com.ubirch.avatar.core.server.util

/**
  * author: cvandrei
  * since: 2016-09-20
  */
object RouteConstants {

  val api = "api"
  val v1 = "v1"
  val avatarService = "avatarService"
  val device = "device"
  val stub = "stub"

  val urlPrefix = s"/$api/$v1/$avatarService"

  val urlDevice = s"$urlPrefix/$device"

  val urlDeviceWithIdPrefix = s"$urlPrefix/$device"
  def urlDeviceWithId(id: String): String = s"$urlDeviceWithIdPrefix/$id"

  val urlDeviceStubWithIdPrefix = s"$urlPrefix/$device/$stub"
  def urlDeviceStubWithId(id: String): String = s"$urlDeviceStubWithIdPrefix/$id"

}
