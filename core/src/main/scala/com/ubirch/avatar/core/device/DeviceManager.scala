package com.ubirch.avatar.core.device

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.awsiot.services.AwsShadowService
import com.ubirch.avatar.awsiot.util.AwsShadowUtil
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.rest.aws.ThingShadowState
import com.ubirch.avatar.model.rest.device.{Device, DeviceInfo}
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.util.elasticsearch.client.binary.storage.ESSimpleStorage
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import org.elasticsearch.index.query.{QueryBuilder, QueryBuilders}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-23
  */
object DeviceManager extends MyJsonProtocol
  with StrictLogging {

  /**
    * Select all devices in any of the given groups.
    *
    * @param groups select devices only if they're in any of these groups
    * @return devices; empty if none found
    */
  def all(groups: Set[UUID]): Future[Seq[Device]] = {

    ESSimpleStorage.getDocs(
      docIndex = Config.esDeviceIndex,
      docType = Config.esDeviceType,
      query = groupsTermsQuery(groups),
      size = Some(Config.esLargePageSize)
    ).map { res =>
      logger.debug(s"all(): result=$res")
      res.map(_.extract[Device])
    }

  }

  /**
    * Select all device stubs in any of the given groups.
    *
    * @param groups select device stubs only if they're in any of these groups
    * @return devices; empty if none found
    */
  def allStubs(groups: Set[UUID]): Future[Seq[DeviceInfo]] = {

    ESSimpleStorage.getDocs(
      docIndex = Config.esDeviceIndex,
      docType = Config.esDeviceType,
      query = groupsTermsQuery(groups),
      size = Some(Config.esLargePageSize)
    ).map { res =>
      res.map { jv =>
        DeviceStubManger.toDeviceInfo(device = jv.extract[Device])
      }
    }
  }

  def create(device: db.device.Device): Future[Option[db.device.Device]] = {

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
        ) map (_.extractOpt[db.device.Device])

      case None =>
        Future(None)
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
        Some(DeviceStubManger.toDeviceInfo(device = device))
      case None =>
        None
    }
  }

  def currentShadowState(device: Device): Option[ThingShadowState] = {
    //TODO fix this
    None
  }

  private def groupsTermsQuery(groups: Set[UUID]): Option[QueryBuilder] = {
    val groupsAsString: Seq[String] = groups.toSeq map (_.toString)
    Some(QueryBuilders.termsQuery("groups", groupsAsString: _*))
  }

}
