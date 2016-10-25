package com.ubirch.avatar.core.device

import com.ubirch.avatar.model.{Device, DummyDevices}
import com.ubirch.services.storage.DeviceStorage
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-23
  */
object DeviceManager extends MyJsonProtocol {

  implicit val ec = scala.concurrent.ExecutionContext.global

  def all(): Future[Seq[Device]] = {
    DeviceStorage.getDocs("devices", "device").map { res =>
      res.map { jv =>
        jv.extract[Device]
      }
    }
  }

  def create(device: Device): Future[Option[Device]] = {
    Json4sUtil.any2jvalue(device) match {
      case Some(devJval) =>
        DeviceStorage.storeDoc("devices", "device", device.deviceId, devJval).map { resJval =>
          Some(resJval.extract[Device])
        }
      case None =>
        Future(None)
    }
  }

  def update(deviceId: String): Option[Device] = DummyDevices.deviceMap.get(deviceId) // TODO implement

  def delete(deviceId: String): Option[Device] = DummyDevices.deviceMap.get(deviceId) // TODO implement

  def info(deviceId: String): Future[Option[Device]] = {
    DeviceStorage.getDoc("devices", "device", deviceId).map {
      case Some(resJval) =>
        Some(resJval.extract[Device])
      case None => None
    }
  }

  def shortInfo(deviceId: String): Option[Device] = DummyDevices.deviceMap.get(deviceId) // TODO implement

}
