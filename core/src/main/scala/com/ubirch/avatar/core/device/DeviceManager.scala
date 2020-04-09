package com.ubirch.avatar.core.device

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.avatar.AvatarStateManager
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.model.rest.device.DeviceInfo
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.util.elasticsearch.client.binary.storage.ESSimpleStorage
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.mongo.connection.MongoUtil
import org.elasticsearch.index.query.{QueryBuilder, QueryBuilders}
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JValue

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

    // TODO automated tests
    ESSimpleStorage.getDocs(
      docIndex = esIndex,
      docType = esType,
      query = groupsTermsQuery(groups),
      size = Some(Config.esLargePageSize)
    ).recover[List[JValue]] {
      case e =>
        logger.error(s"error fetching device all for groups $groups", e)
        List()
    }.map { res =>
      logger.debug(s"all(): result=$res")
      if (res.nonEmpty)
        res.map(_.extract[Device])
      else
        Seq()
    }
  }

  /**
    * Select all devices in any of the given groups or with the userId as owner.
    *
    * @param userId the id of the owner
    * @param groups select devices only if they're in any of these groups
    * @return devices; empty if none found
    */
  def all(userId: UUID, groups: Set[UUID]): Future[Seq[Device]] = {

    // TODO automated tests
    ESSimpleStorage.getDocs(
      docIndex = esIndex,
      docType = esType,
      query = groupsUserTermsQuery(userId, groups),
      size = Some(Config.esLargePageSize)
    ).recover[List[JValue]] {
      case e =>
        logger.error(s"error fetching device all for groups $groups", e)
        List()
    }.map { res =>
      logger.debug(s"all(): result=$res")
      if (res.nonEmpty)
        res.map(_.extract[Device])
      else
        Seq()
    }
  }


  def all(): Future[Seq[Device]] = {
    // TODO automated tests
    ESSimpleStorage.getDocs(
      docIndex = esIndex,
      docType = esType,
      query = None,
      size = Some(Config.esLargePageSize)
    ).recover[List[JValue]] {
      case e =>
        logger.error(s"error fetching all devices", e)
        List()
    }.map { res =>
      logger.debug(s"all(): result=$res")
      if (res.nonEmpty)
        res.map(_.extract[Device])
      else
        Seq()
    }
  }

  /**
    * Select all device stubs in any of the given groups.
    *
    * @param groups select device stubs only if they're in any of these groups
    * @return devices; empty if none found
    */
  def allStubs(userId: UUID, groups: Set[UUID]): Future[Seq[DeviceInfo]] = {

    // TODO automated tests
    ESSimpleStorage.getDocs(
      docIndex = esIndex,
      docType = esType,
      query = groupsUserTermsQuery(userId, groups),
      size = Some(Config.esLargePageSize)
    ).recover[List[JValue]] {
      case e =>
        logger.error(s"error fetching devices allStubs for groups $groups", e)
        List()
    }.map { res =>
      if (res.nonEmpty)
        res.map { jv =>
          DeviceStubManger.toDeviceInfo(device = jv.extract[Device])
        }
      else
        Seq()
    }
  }

  def create(device: db.device.Device): Future[Option[db.device.Device]] = {

    val deviceToStore = device.copy(hwDeviceId = device.hwDeviceId.toLowerCase)

    for {

      existingId <- info(deviceToStore.deviceId)
      existingHwDeviceId <- infoByHwId(deviceToStore.hwDeviceId)

      created <- createWithChecks(
        existingId = existingId,
        existingHwDeviceId = existingHwDeviceId,
        deviceToStore
      )

    } yield created

  }

  def update(device: Device)(implicit mongo: MongoUtil): Future[Option[Device]] = {

    for {

      existingId <- info(device.deviceId)
      existingHwDeviceId <- infoByHwId(device.hwDeviceId)

      updated <- updateWithChecks(existingId: Option[Device], existingHwDeviceId: Option[Device], device)

    } yield updated

  }

  def delete(device: Device): Future[Option[Device]] = {

    // TODO automated tests
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

    // TODO automated tests
    // TODO test case: hwDevice exist w/ lowercase and uppercase
    val query = QueryBuilders.termQuery("hwDeviceId", hwDeviceId.toLowerCase)
    ESSimpleStorage.getDocs(
      docIndex = esIndex,
      docType = esType,
      query = Some(query)
    ).recover[List[JValue]] {
      case e =>
        logger.error(s"error fetching device infoByHwId for $hwDeviceId", e)
        List()
    }.map { doc =>

      doc.headOption match {
        case Some(jval) => jval.extractOpt[Device]
        case None => None
      }
    }
  }

  def infoByHashedHwId(hashedHwDeviceId: String): Future[Option[Device]] = {

    // TODO automated tests
    logger.debug(s"starting infoByHashedHwId($hashedHwDeviceId)")
    val query = QueryBuilders.termQuery("hashedHwDeviceId", hashedHwDeviceId)
    val start = System.currentTimeMillis()
    ESSimpleStorage.getDocs(
      docIndex = esIndex,
      docType = esType,
      query = Some(query)
    ).recover[List[JValue]] {
      case e =>
        logger.debug(s"query $query took ${System.currentTimeMillis() - start}ms")
        logger.error(s"error fetching device infoByHashedHwId for $hashedHwDeviceId", e)
        List()
    }.map { docs =>
      logger.debug(s"query $hashedHwDeviceId: took ${System.currentTimeMillis() - start}ms")
      logger.debug(s"found ${docs.size} results")
      docs.headOption match {
        case Some(jval) =>
          logger.debug(s"extracted device: ${jval.extractOpt[Device]}")
          jval.extractOpt[Device]
        case None => None
      }
    }
  }


  // TODO automated tests
  def info(deviceId: UUID): Future[Option[Device]] = info(deviceId.toString)

  def info(deviceId: String): Future[Option[Device]] = {

    // TODO automated tests
    ESSimpleStorage.getDoc(
      docIndex = esIndex,
      docType = esType,
      docId = deviceId
    ).recover[Option[JValue]] {
      case e =>
        logger.error(s"error fetching device info for $deviceId", e)
        None
    }.map {
      case Some(jval) => jval.extractOpt[Device]
      case None => None
    }

  }

  def stub(deviceId: UUID): Future[Option[DeviceInfo]] = {

    // TODO automated tests
    info(deviceId).map {
      case Some(device) => Some(DeviceStubManger.toDeviceInfo(device = device))
      case None => None
    }

  }

  private def groupsUserTermsQuery(userId: UUID, groups: Set[UUID]): Option[QueryBuilder] = {
    val groupsAsString: Seq[String] = groups.toSeq map (_.toString)
    val userIdAsString: String = userId.toString
    Some(QueryBuilders.boolQuery()
      .should(QueryBuilders.termsQuery("groups", groupsAsString: _*))
      .should(QueryBuilders.termsQuery("owners", userIdAsString))
      .minimumShouldMatch(1)
    )
  }

  private def groupsTermsQuery(groups: Set[UUID]): Option[QueryBuilder] = {
    val groupsAsString: Seq[String] = groups.toSeq map (_.toString)
    Some(QueryBuilders.termsQuery("groups", groupsAsString: _*))
  }

  private def createWithChecks(existingId: Option[Device],
                               existingHwDeviceId: Option[Device],
                               deviceToStore: Device
                              ): Future[Option[Device]] = {

    if (existingId.isDefined) {

      logger.error(s"deviceId exists already: deviceId=${deviceToStore.deviceId}")
      Future(None)

    } else if (existingHwDeviceId.isDefined) {

      logger.error(s"hwDeviceId exists already: hwDeviceId=${deviceToStore.hwDeviceId}")
      Future(None)

    } else {

      val devWithDefaults = DeviceUtil.deviceWithDefaults(deviceToStore)
      Json4sUtil.any2jvalue(devWithDefaults) match {

        case Some(devJval) =>
          ESSimpleStorage.storeDoc(
            docIndex = esIndex,
            docType = esType,
            docIdOpt = Some(deviceToStore.deviceId),
            doc = devJval
          ) map (_.extractOpt[db.device.Device])

        case None => Future(None)
      }

    }

  }

  private def updateWithChecks(existingIdOpt: Option[Device],
                               existingHardwareIdOpt: Option[Device],
                               device: Device
                              )(implicit mongo: MongoUtil): Future[Option[Device]] = {

    if (existingIdOpt.isEmpty) {

      logger.error(s"deviceId does not exist: deviceId=${device.deviceId}")
      Future(None)

    } else if (existingHardwareIdOpt.isEmpty) {

      logger.error(s"hwDeviceId does not exist: hwDeviceId=${device.hwDeviceId}")
      Future(None)

    } else if (existingIdOpt.get.deviceId == existingHardwareIdOpt.get.deviceId &&
      existingIdOpt.get.hwDeviceId == existingHardwareIdOpt.get.hwDeviceId
    ) {

      if (existingIdOpt.get.hashedHwDeviceId != device.hashedHwDeviceId) {
        logger.error(s"someone tried to change the hashedHwDeviceId: deviceId=${device.deviceId}, hwDeviceId=${device.hashedHwDeviceId}")
        Future(None)
      } else if (existingIdOpt.get.created != device.created) {
        logger.error(s"someone tried to change the created field: deviceId=${device.deviceId}, created=${device.hashedHwDeviceId}")
        Future(None)
      } else {

        val toUpdate = device.copy(
          hwDeviceId = device.hwDeviceId.toLowerCase,
          hashedHwDeviceId = existingIdOpt.get.hashedHwDeviceId,
          created = existingIdOpt.get.created,
          deviceConfig = device.deviceConfig,
          updated = Some(DateTime.now(DateTimeZone.UTC))
        )
        Json4sUtil.any2jvalue(toUpdate) match {

          case Some(devJval) =>

            val dev = ESSimpleStorage.storeDoc(
              docIndex = esIndex,
              docType = esType,
              docIdOpt = Some(toUpdate.deviceId),
              doc = devJval
            ).map(_.extractOpt[Device])

            if (device.deviceConfig.isDefined) {
              AvatarStateManager.setDesired(toUpdate, toUpdate.deviceConfig.get)
            }

            dev

          case None => Future(None)

        }

      }

    } else {

      logger.error(s"someone tried to change a device's (hardware) id to: deviceId=${device.deviceId}, hwDeviceId=${device.hwDeviceId}")
      Future(None)

    }

  }

  def checkProperty(device: Device, propertyKey: String): Boolean = {
    if (device.deviceConfig.isDefined) {
      (device.deviceProperties.get.camelizeKeys \ propertyKey).extractOpt[String] match {
        case None =>
          (device.deviceProperties.get.camelizeKeys \ propertyKey).extractOpt[Boolean] match {
            case Some(boolVal) =>
              boolVal
            case None =>
              false
          }
        case Some(boolVal) =>
          if (boolVal.trim.toLowerCase == "true")
            true
          else
            false
      }
    }
    else
      false
  }
}
