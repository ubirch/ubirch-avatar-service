package com.ubirch.avatar.core.device

import java.util.UUID

import com.ubirch.avatar.awsiot.services.AwsShadowService
import com.ubirch.avatar.awsiot.util.AwsShadowUtil
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.aws.ThingShadowState
import com.ubirch.avatar.model.device.Device
import com.ubirch.services.storage.DeviceStorage
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import org.elasticsearch.index.query.QueryBuilders

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

        AwsShadowUtil.createShadow(device.awsDeviceThingId)

        Some(device)

      case None =>
        None
    }
  }

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

  def delete(device: Device): Future[Option[Device]] = {

    AwsShadowUtil.deleteShadow(device.awsDeviceThingId)

    DeviceStorage.deleteDoc(Config.esDeviceIndex, Config.esDeviceType, device.deviceId).map {
      case true =>

        Some(device)

      case _ =>
        None
    }
  }

  def infoByHwId(hwDeviceId: String): Future[Option[Device]] = {
    val query = QueryBuilders.termQuery("hwDeviceId", hwDeviceId)
    DeviceStorage.getDocs(Config.esDeviceIndex, Config.esDeviceType, query = Some(query)).map { l =>
      l.headOption match {
        case Some(jval) =>
          jval.extractOpt[Device]
        case None =>
          None
      }
    }
  }


  def info(deviceId: UUID): Future[Option[Device]] = {
    info(deviceId.toString)
  }

  def info(deviceId: String): Future[Option[Device]] = {
    DeviceStorage.getDoc(Config.esDeviceIndex, Config.esDeviceType, deviceId).map {
      case Some(resJval) =>
        Some(resJval.extract[Device])
      case None => None
    }
  }

  def shortInfo(deviceId: String): Future[Option[Device]] = info(deviceId: String) // TODO implement

  def curretShadowState(device: Device): ThingShadowState = {
    AwsShadowService.getCurrentDeviceState(device.awsDeviceThingId)
  }

}
