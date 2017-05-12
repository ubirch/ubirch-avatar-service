package com.ubirch.avatar.core.device

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.awsiot.services.AwsShadowService
import com.ubirch.avatar.awsiot.util.AwsShadowUtil
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.rest.aws.ThingShadowState
import com.ubirch.avatar.model.rest.device.{Device, DeviceInfo}
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.elasticsearch.client.binary.storage.ESSimpleStorage
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import org.elasticsearch.index.query.QueryBuilders

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-23
  */
object DeviceManager extends MyJsonProtocol
  with StrictLogging {

  def all(): Future[Seq[Device]] = {
    ESSimpleStorage.getDocs(Config.esDeviceIndex, Config.esDeviceType).map { res =>
      res.map(_.extract[Device])
    }
  }

  def allStubs(): Future[Seq[DeviceInfo]] = {
    ESSimpleStorage.getDocs(docIndex = Config.esDeviceIndex, docType = Config.esDeviceType, size = Some(100)).map { res =>
      res.map { jv =>
        DeviceStubManger.create(device = jv.extract[Device])
      }
    }
  }

  def create(device: Device): Future[Option[Device]] = {

    val devWithDefaults = device.copy(
      hashedHwDeviceId = HashUtil.sha512Base64(device.hwDeviceId),
      deviceProperties = Some(device.deviceProperties.getOrElse(
        DeviceTypeUtil.defaultProps(device.deviceTypeKey)
      )),
      deviceConfig = Some(device.deviceConfig.getOrElse(
        DeviceTypeUtil.defaultConf(device.deviceTypeKey)
      )),
      tags = if (device.tags.isEmpty)
        DeviceTypeUtil.defaultTags(device.deviceTypeKey)
      else device.tags
    )

    Json4sUtil.any2jvalue(devWithDefaults) match {

      case Some(devJval) =>
        ESSimpleStorage.storeDoc(
          docIndex = Config.esDeviceIndex,
          docType = Config.esDeviceType,
          docIdOpt = Some(device.deviceId),
          doc = devJval
        ) map (_.extractOpt[Device])

      case None =>
        Future(None)
    }
  }

  def createWithShadow(device: Device): Future[Option[Device]] = {
    create(device: Device).map {
      case Some(dev) =>

        try {
          AwsShadowUtil.createShadow(dev.awsDeviceThingId)
          if (dev.deviceConfig.isDefined)
            AwsShadowUtil.setDesired(dev, dev.deviceConfig.get)
        }
        catch {
          case e: Exception =>
            logger.error("could not create a shadow", e)
        }

        Some(dev)

      case None =>
        None
    }
  }

  def update(device: Device): Future[Option[Device]] = {

    Json4sUtil.any2jvalue(device) match {

      case Some(devJval) =>
        val dev = ESSimpleStorage.storeDoc(
          docIndex = Config.esDeviceIndex,
          docType = Config.esDeviceType,
          docIdOpt = Some(device.deviceId),
          doc = devJval
        ).map(_.extractOpt[Device])

        if (device.deviceConfig.isDefined)
          AwsShadowUtil.setDesired(device, device.deviceConfig.get)

        dev
      case None =>
        Future(None)
    }
  }

  def delete(device: Device): Future[Option[Device]] = {

    AwsShadowUtil.deleteShadow(device.awsDeviceThingId)

    ESSimpleStorage.deleteDoc(Config.esDeviceIndex, Config.esDeviceType, device.deviceId).map {
      case true =>

        Some(device)

      case _ =>
        None
    }
  }

  def infoByHwId(hwDeviceId: String): Future[Option[Device]] = {
    val query = QueryBuilders.termQuery("hwDeviceId", hwDeviceId)
    ESSimpleStorage.getDocs(Config.esDeviceIndex, Config.esDeviceType, query = Some(query)).map { l =>
      l.headOption match {
        case Some(jval) =>
          jval.extractOpt[Device]
        case None =>
          None
      }
    }
  }

  def infoByHashedHwId(hashedHwDeviceId: String): Future[Option[Device]] = {
    val query = QueryBuilders.termQuery("hashedHwDeviceId", hashedHwDeviceId)
    ESSimpleStorage.getDocs(Config.esDeviceIndex, Config.esDeviceType, query = Some(query)).map { l =>
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
    ESSimpleStorage.getDoc(Config.esDeviceIndex, Config.esDeviceType, deviceId).map {
      case Some(resJval) =>
        Some(resJval.extract[Device])
      case None =>
        None
    }
  }

  def stub(deviceId: UUID): Future[Option[DeviceInfo]] = {
    info(deviceId).map {
      case Some(device) =>
        Some(DeviceStubManger.create(device = device))
      case None =>
        None
    }
  }

  def curretShadowState(device: Device): Option[ThingShadowState] = {
    AwsShadowService.getCurrentDeviceState(device.awsDeviceThingId)
  }

}
