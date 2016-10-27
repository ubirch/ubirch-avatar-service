package com.ubirch.avatar.core.device

import com.ubirch.avatar.backend.aws.services.ShadowService
import com.ubirch.avatar.backend.aws.util.AwsThingUtil
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.Device
import com.ubirch.avatar.model.aws.ThingShadowState
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
    DeviceStorage.getDocs(Config.esDeviceIndex, Config.esDeviceType).map { res =>
      res.map { jv =>
        jv.extract[Device]
      }
    }
  }

  def create(device: Device): Future[Option[Device]] = {
    Json4sUtil.any2jvalue(device) match {
      case Some(devJval) =>
        DeviceStorage.storeDoc(Config.esDeviceIndex, Config.esDeviceType, Some(device.deviceId), devJval).map { resJval =>
          Some(resJval.extract[Device])

        }
      case None =>
        Future(None)
    }
  }

  def createWithShadow(device: Device): Future[Option[Device]] = {
    create(device: Device).map {
      case Some(device) =>
        AwsThingUtil.createShadow(device.awsDeviceThingId)
        Some(device)
      case None => None
    }
  }

  //  def update(deviceId: String): Option[Device] = DummyDevices.deviceMap.get(deviceId) // TODO implement

  def update(device: Device): Future[Option[Device]] = {
    Json4sUtil.any2jvalue(device) match {
      case Some(devJval) =>
        DeviceStorage.storeDoc(Config.esDeviceIndex, Config.esDeviceType, Some(device.deviceId), devJval).map { resJval =>
          Some(resJval.extract[Device])
        }
      case None =>
        Future(None)
    }
  }

  //  def delete(deviceId: String): Option[Device] = {
  //    DummyDevices.deviceMap.get(deviceId) // TODO implement
  //  }

  def delete(device: Device): Future[Option[Device]] = {
    AwsThingUtil.deleteShadow(device.awsDeviceThingId)
    DeviceStorage.deleteDoc(Config.esDeviceIndex, Config.esDeviceType, device.deviceId).map {
      case true =>
        Some(device)
      case _ =>
        None
    }
  }

  def info(deviceId: String): Future[Option[Device]] = {
    DeviceStorage.getDoc(Config.esDeviceIndex, Config.esDeviceType, deviceId).map {
      case Some(resJval) =>
        Some(resJval.extract[Device])
      case None => None
    }
  }

  def shortInfo(deviceId: String): Option[Device] = ??? // TODO implement

  def curretShadowState(device: Device): ThingShadowState = {
    ShadowService.getCurrentDeviceState(device.awsDeviceThingId)
  }

}
