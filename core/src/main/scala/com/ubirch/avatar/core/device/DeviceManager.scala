package com.ubirch.avatar.core.device

import com.ubirch.avatar.model.{DummyDevices, Device}

/**
  * author: cvandrei
  * since: 2016-09-23
  */
object DeviceManager {

  def all(): Seq[Device] = DummyDevices.all // TODO implement

  def create(device: Device): Option[Device] = Some(device) // TODO implement

  def update(deviceId: String): Option[Device] = DummyDevices.deviceMap.get(deviceId) // TODO implement

  def delete(deviceId: String): Option[Device] = DummyDevices.deviceMap.get(deviceId) // TODO implement

  def info(deviceId: String): Option[Device] = DummyDevices.deviceMap.get(deviceId) // TODO implement

  def shortInfo(deviceId: String): Option[Device] = DummyDevices.deviceMap.get(deviceId) // TODO implement

}
