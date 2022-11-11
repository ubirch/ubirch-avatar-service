package com.ubirch.avatar.core.device

import co.elastic.clients.elasticsearch._types.query_dsl.{ BoolQuery, Query }
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.avatar.AvatarStateManager
import com.ubirch.avatar.model._
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.util.model.DeviceUtil
import com.ubirch.util.elasticsearch.EsSimpleClient
import com.ubirch.util.elasticsearch.util.QueryUtil
import com.ubirch.util.json.{ Json4sUtil, MyJsonProtocol }
import com.ubirch.util.mongo.connection.MongoUtil
import org.joda.time.{ DateTime, DateTimeZone }
import org.json4s.JValue

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-23
  */
object DeviceManager extends MyJsonProtocol with StrictLogging {

  private val esIndex = Config.esDeviceIndex

  /**
    * Select all devices in any of the given groups.
    *
    * @param groups select devices only if they're in any of these groups
    * @return devices; empty if none found
    */
  def all(groups: Set[UUID]): Future[Seq[Device]] = {

    // TODO automated tests
    EsSimpleClient.getDocs(
      docIndex = esIndex,
      query = Some(QueryUtil.buildTermsQuery("groups", groups.map(_.toString))),
      size = Config.esLargePageSize
    ).recover {
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
    EsSimpleClient.getDocs(
      docIndex = esIndex,
      query = groupsUserTermsQuery(userId, groups),
      size = Config.esLargePageSize
    ).recover {
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
    EsSimpleClient.getDocs(
      docIndex = esIndex,
      query = None,
      size = Config.esLargePageSize
    ).recover {
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

  def create(device: db.device.Device, waitingForRefresh: Boolean = false): Future[Option[db.device.Device]] = {

    val deviceToStore = device.copy(hwDeviceId = device.hwDeviceId.toLowerCase)

    for {

      existingId <- info(deviceToStore.deviceId)
      existingHwDeviceId <- infoByHwId(deviceToStore.hwDeviceId)

      created <- createWithChecks(
        existingId = existingId,
        existingHwDeviceId = existingHwDeviceId,
        deviceToStore,
        waitingForRefresh = waitingForRefresh
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
    EsSimpleClient.deleteDoc(
      docIndex = esIndex,
      field = "deviceId",
      value = device.deviceId
    ).map(_ => Some(device)).recover {
      case ex =>
        logger.error(s"error deleting device $device", ex)
        None
    }

  }

  def infoByHwId(hwDeviceId: String): Future[Option[Device]] = {

    // TODO automated tests
    // TODO test case: hwDevice exist w/ lowercase and uppercase
    val query = QueryUtil.buildTermQuery("hwDeviceId", hwDeviceId.toLowerCase)
    EsSimpleClient.getDocs(
      docIndex = esIndex,
      query = Some(query)
    ).map { doc =>
      doc.headOption match {
        case Some(jval) => jval.extractOpt[Device]
        case None       => None
      }
    }.recover {
      case e =>
        logger.error(s"error parsing device from jValue for $hwDeviceId", e)
        None
    }
  }

  def infoByHashedHwId(hashedHwDeviceId: String): Future[Option[Device]] = {

    // TODO automated tests
    logger.debug(s"starting infoByHashedHwId($hashedHwDeviceId)")
    val query = QueryUtil.buildTermQuery("hashedHwDeviceId", hashedHwDeviceId)
    val start = System.currentTimeMillis()
    EsSimpleClient.getDocs(
      docIndex = esIndex,
      query = Some(query)
    ).recover {
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
    EsSimpleClient.getDoc(
      docIndex = esIndex,
      docId = deviceId
    ).recover[Option[JValue]] {
      case e =>
        logger.error(s"error fetching device info for $deviceId", e)
        None
    }.map {
      case Some(jval) => jval.extractOpt[Device]
      case None       => None
    }

  }

  //Todo: check, if owners query is correct!
  private def groupsUserTermsQuery(userId: UUID, groups: Set[UUID]): Option[Query] = {
    val groupTermsQ: Query = QueryUtil.buildTermsQuery("groups", groups.map(_.toString))
    val userTermsQ: Query = QueryUtil.buildTermsQuery("owners", Set(userId.toString))
    val queries = new java.util.ArrayList[Query]()
    queries.add(groupTermsQ)
    queries.add(userTermsQ)
    val boolQuery = new BoolQuery.Builder().should(queries).minimumShouldMatch("1").build
    Some(new Query.Builder().bool(boolQuery).build())
  }

  private def createWithChecks(
    existingId: Option[Device],
    existingHwDeviceId: Option[Device],
    deviceToStore: Device,
    waitingForRefresh: Boolean = false): Future[Option[Device]] = {

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
          EsSimpleClient.storeDoc(
            docIndex = esIndex,
            docIdOpt = Some(deviceToStore.deviceId),
            doc = devJval,
            waitingForRefresh = waitingForRefresh
          ).map(_ => devJval.extractOpt[Device])
            .recover {
              case ex =>
                logger.error(s"error storing document $devJval in index=$esIndex with id=${deviceToStore.deviceId}", ex)
                None
            }

        case None => Future(None)
      }

    }

  }

  private def updateWithChecks(
    existingIdOpt: Option[Device],
    existingHardwareIdOpt: Option[Device],
    device: Device)(implicit mongo: MongoUtil): Future[Option[Device]] = {

    if (existingIdOpt.isEmpty) {

      logger.error(s"deviceId does not exist: deviceId=${device.deviceId}")
      Future(None)

    } else if (existingHardwareIdOpt.isEmpty) {

      logger.error(s"hwDeviceId does not exist: hwDeviceId=${device.hwDeviceId}")
      Future(None)

    } else if (existingIdOpt.get.deviceId == existingHardwareIdOpt.get.deviceId &&
      existingIdOpt.get.hwDeviceId == existingHardwareIdOpt.get.hwDeviceId) {

      if (existingIdOpt.get.hashedHwDeviceId != device.hashedHwDeviceId) {
        logger.error(s"someone tried to change the hashedHwDeviceId: deviceId=${device.deviceId}, hwDeviceId=${device.hashedHwDeviceId}")
        Future(None)
      } else if (existingIdOpt.get.created != device.created) {
        logger.error(
          s"someone tried to change the created field: deviceId=${device.deviceId}, created=${device.hashedHwDeviceId}")
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
            val dev = EsSimpleClient.storeDoc(
              docIndex = esIndex,
              docIdOpt = Some(toUpdate.deviceId),
              doc = devJval
            ).map(_ => devJval.extractOpt[Device])
              .recover {
                case ex =>
                  logger.error(s"error storing document $devJval in index=$esIndex with id=${toUpdate.deviceId}", ex)
                  None
              }

            if (device.deviceConfig.isDefined) {
              AvatarStateManager.setDesired(toUpdate, toUpdate.deviceConfig.get)
            }

            dev

          case None => Future(None)

        }

      }

    } else {

      logger.error(
        s"someone tried to change a device's (hardware) id to: deviceId=${device.deviceId}, hwDeviceId=${device.hwDeviceId}")
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
    } else
      false
  }
}
