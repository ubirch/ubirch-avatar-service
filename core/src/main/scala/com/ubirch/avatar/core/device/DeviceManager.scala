package com.ubirch.avatar.core.device

import com.ubirch.avatar.model.{Device, DummyDevices}
import com.ubirch.services.storage.ElasticsearchStorage
import com.ubirch.util.json.Json4sUtil
import org.json4s.DefaultFormats

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-23
  */
object DeviceManager {

  implicit val formats = DefaultFormats.lossless ++ org.json4s.ext.JodaTimeSerializers.all
  implicit val ec = scala.concurrent.ExecutionContext.global

  def all(): Future[Seq[Device]] = {
    ElasticsearchStorage.getDocs("devices", "device").map { res =>
      res.map { jv =>
        jv.extract[Device]
      }
    }
  }

  def create(device: Device): Future[Option[Device]] = {
    Json4sUtil.any2jvalue(device) match {
      case Some(devJval) =>
        ElasticsearchStorage.storeDoc("devices", "device", device.deviceId, devJval).map { resJval =>
          Some(resJval.extract[Device])
        }
      case None =>
        Future(None)
    }
  }

  def update(deviceId: String): Option[Device] = DummyDevices.deviceMap.get(deviceId) // TODO implement

  def delete(deviceId: String): Option[Device] = DummyDevices.deviceMap.get(deviceId) // TODO implement

  def info(deviceId: String): Future[Option[Device]] = {
    ElasticsearchStorage.getDoc("devices", "device", deviceId).map {
      case Some(resJval) =>
        Some(resJval.extract[Device])
      case None => None
    }
  }

  def shortInfo(deviceId: String): Option[Device] = DummyDevices.deviceMap.get(deviceId) // TODO implement

}
