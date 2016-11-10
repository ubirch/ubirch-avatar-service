package com.ubirch.avatar.core.device

import com.typesafe.scalalogging.LazyLogging

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.DeviceType
import com.ubirch.services.storage.DeviceTypeStorage
import com.ubirch.services.util.DeviceUtil
import com.ubirch.util.json.{Json4sUtil, JsonFormats}

import org.elasticsearch.index.query.QueryBuilders
import org.json4s.Formats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-11-09
  */
object DeviceTypeManager extends LazyLogging {

  private implicit def formats: Formats = JsonFormats.default

  private val index = Config.esDeviceTypeIndex
  private val esType = Config.esDeviceTypeType

  /**
    * @return all existing [[DeviceType]]s; empty if none exist
    */
  def all(): Future[Seq[DeviceType]] = {

    DeviceTypeStorage.getDocs(index, esType) map { res =>
      res.map(_.extract[DeviceType])
    }

  }

  /**
    * Search for a [[DeviceType]] based on it's key.
    *
    * @param key search criteria
    * @return None if nothing is found, otherwise Some
    */
  def getByKey(key: String): Future[Option[DeviceType]] = {

    val query = Some(QueryBuilders.termQuery("key", key))

    DeviceTypeStorage.getDocs(index, esType, query) map { res =>
      res.map(_.extract[DeviceType]).headOption
    }

  }

  /**
    * Create a [[DeviceType]].
    *
    * @param deviceType deviceType to persist
    * @return deviceType that was just created; None if it already existed or something went wrong
    */
  def create(deviceType: DeviceType): Future[Option[DeviceType]] = {

    val key = deviceType.key
    Json4sUtil.any2jvalue(deviceType) match {

      case Some(doc) =>
        getByKey(key) flatMap {

          case Some(dbRecord) => Future(None)

          case None =>
            DeviceTypeStorage.storeDoc(
              docIndex = index,
              docType = esType,
              docIdOpt = Some(key),
              doc = doc
            ) map { jv =>
              Some(jv.extract[DeviceType])
            }

        }

      case None =>
        logger.error(s"failed to convert DeviceType to JSON: deviceType=$deviceType")
        Future(None)
    }

  }

  /**
    * Update a [[DeviceType]].
    *
    * @param deviceType deviceType to update
    * @return updated deviceType; None if no record exists or something went wrong
    */
  def update(deviceType: DeviceType): Future[Option[DeviceType]] = {

    val key = deviceType.key
    Json4sUtil.any2jvalue(deviceType) match {

      case Some(doc) =>
        getByKey(key) flatMap {

          case Some(dbRecord) =>
            DeviceTypeStorage.storeDoc(
              docIndex = index,
              docType = esType,
              docIdOpt = Some(key),
              doc = doc
            ) map { jv =>
              Some(jv.extract[DeviceType])
            }

          case None => Future(None)

        }

      case None =>
        logger.error(s"failed to convert DeviceType to JSON: deviceType=$deviceType")
        Future(None)
    }

  }

  /**
    * Check the database for device types and if none exist create the default types instead.
    *
    * @return deviceTypes currently in the database; never empty unless database is empty and the list of default deviceTypes is empty, too
    */
  def init(): Future[Seq[DeviceType]] = {

    all() map { allTypes =>

      allTypes.isEmpty match {

        case true =>
          val defaultTypes = DeviceUtil.defaultDeviceTypes.toSeq
          defaultTypes foreach create
          defaultTypes

        case false => allTypes

      }

    }

  }

}
