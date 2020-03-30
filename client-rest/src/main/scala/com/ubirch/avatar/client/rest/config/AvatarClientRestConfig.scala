package com.ubirch.avatar.client.rest.config

import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.config.ConfigBase

/**
  * author: cvandrei
  * since: 2018-10-17
  */
object AvatarClientRestConfig extends ConfigBase {

  private def host = config.getString(AvatarClientRestConfigKeys.HOST)

  val urlCheck = s"$host${RouteConstants.pathCheck}"

  val urlDeepCheck = s"$host${RouteConstants.pathDeepCheck}"

  val urlDevice = s"$host${RouteConstants.pathDevice}"

  val urlDeviceClaim = s"$host${RouteConstants.pathDeviceClaim}"

  val urlDeviceStub = s"$host${RouteConstants.pathDeviceStub}"

  val urlDeviceUpdate = s"$host${RouteConstants.pathDeviceUpdate}"

  val urlDeviceUpdateBulk = s"$host${RouteConstants.pathDeviceBulk}"

  def urlDeviceWithId(deviceId: String) = s"$host${RouteConstants.pathDeviceWithId(deviceId)}"

  def urlDataTransferDates(deviceId: String) = s"$host${RouteConstants.pathDeviceDataTransferDates(deviceId)}"
}
