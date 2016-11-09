package com.ubirch.avatar.core.device

import com.ubirch.avatar.model.device.DeviceType
import com.ubirch.services.util.DeviceUtil

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2016-11-09
  */
object DeviceTypeManager {

  // TODO scaladoc for all methods

  def all(): Future[Set[DeviceType]] = Future(DeviceUtil.defaultDeviceTypes) // TODO implementation

  def create(deviceType: DeviceType): Future[Option[DeviceType]] = Future(Some(deviceType)) // TODO implementation

  def update(deviceType: DeviceType): Future[Option[DeviceType]] = Future(Some(deviceType)) // TODO implementation

  def init(): Future[Set[DeviceType]] = Future(DeviceUtil.defaultDeviceTypes) // TODO implementation

}
