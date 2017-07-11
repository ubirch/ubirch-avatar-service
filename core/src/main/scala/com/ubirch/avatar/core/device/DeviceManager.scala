package com.ubirch.avatar.core.device

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.avatar.AvatarStateManager
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.aws.ThingShadowState
import com.ubirch.avatar.model.rest.device.DeviceInfo
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.util.elasticsearch.client.binary.storage.ESSimpleStorage
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.mongo.connection.MongoUtil

import org.elasticsearch.index.query.{QueryBuilder, QueryBuilders}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-23
  */
object DeviceManager
  extends MyJsonProtocol
    with StrictLogging {

  private val esIndex = Config.esDeviceIndex
  private val esType = Config.esDeviceType

  /**
    * Select all devices in any of the given groups.
    *
    * @param groups select devices only if they're in any of these groups
    * @return devices; empty if none found
    */
  def all(groups: Set[UUID]): Future[Seq[Device]] = {

    ESSimpleStorage.getDocs(
      docIndex = esIndex,
      docType = esType,
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
      docIndex = esIndex,
      docType = esType,
      query = groupsTermsQuery(groups),
      size = Some(Config.esLargePageSize)
    ).map { res =>
      res.map { jv =>
        DeviceStubManger.toDeviceInfo(device = jv.extract[Device])
      }
    }
  }

  def create(device: db.device.Device): Future[Option[db.device.Device]] = {

    infoByHwId(device.hwDeviceId) flatMap {

      case Some(_: db.device.Device) =>
        logger.error(s"device with hwDeviceId already exists: hwDeviceId=${device.hwDeviceId}")
        Future(None)

      case None =>

        val devWithDefaults = DeviceUtil.deviceWithDefaults(device)
        Json4sUtil.any2jvalue(devWithDefaults) match {

          case Some(devJval) =>
            ESSimpleStorage.storeDoc(
              docIndex = esIndex,
              docType = esType,
              docIdOpt = Some(device.deviceId),
              doc = devJval
            ) map (_.extractOpt[db.device.Device])

          case None => Future(None)
        }

    }

  }

  def update(device: Device)(implicit mongo: MongoUtil): Future[Option[Device]] = {

    Json4sUtil.any2jvalue(device) match {

      case Some(devJval) =>

        val dev = ESSimpleStorage.storeDoc(
          docIndex = esIndex,
          docType = esType,
          docIdOpt = Some(device.deviceId),
          doc = devJval
        ).map(_.extractOpt[Device])

        if (device.deviceConfig.isDefined) {
          AvatarStateManager.setDesired(device, device.deviceConfig.get)
        }

        dev

      case None => Future(None)

    }

  }

  def delete(device: Device): Future[Option[Device]] = {

    ESSimpleStorage.deleteDoc(
      docIndex = esIndex,
      docType = esType,
      docId = device.deviceId
    ).map {
      case true => Some(device)
      case _ => None
    }

  }

  def infoByHwId(hwDeviceId: String): Future[Option[Device]] = {

    val query = QueryBuilders.termQuery("hwDeviceId", hwDeviceId)
    ESSimpleStorage.getDocs(
      docIndex = esIndex,
      docType = esType,
      query = Some(query)
    ).map {

      _.headOption match {
        case Some(jval) => jval.extractOpt[Device]
        case None => None
      }

    }

  }

  def infoByHashedHwId(hashedHwDeviceId: String): Future[Option[Device]] = {

    val query = QueryBuilders.termQuery("hashedHwDeviceId", hashedHwDeviceId)
    ESSimpleStorage.getDocs(
      docIndex = esIndex,
      docType = esType,
      query = Some(query)
    ).map {

      _.headOption match {
        case Some(jval) => jval.extractOpt[Device]
        case None => None
      }

    }
  }


  def info(deviceId: UUID): Future[Option[Device]] = info(deviceId.toString)

  def info(deviceId: String): Future[Option[Device]] = {

    ESSimpleStorage.getDoc(
      docIndex = esIndex,
      docType = esType,
      docId = deviceId
    ).map {
      case Some(resJval) => Some(resJval.extract[Device])
      case None => None
    }

  }

  def stub(deviceId: UUID): Future[Option[DeviceInfo]] = {

    info(deviceId).map {
      case Some(device) => Some(DeviceStubManger.toDeviceInfo(device = device))
      case None => None
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
