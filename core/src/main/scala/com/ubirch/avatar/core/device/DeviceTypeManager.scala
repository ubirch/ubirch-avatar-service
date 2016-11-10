package com.ubirch.avatar.core.device

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.DeviceType
import com.ubirch.services.storage.DeviceTypeStorage
import com.ubirch.services.util.DeviceUtil
import com.ubirch.util.json.JsonFormats

import org.json4s.Formats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-11-09
  */
object DeviceTypeManager {

  private implicit def formats: Formats = JsonFormats.default

  /**
    * @return all existing [[DeviceType]]s; empty if none exist
    */
  def all(): Future[Seq[DeviceType]] = {

    // TODO integration tests
    val index = Config.esDeviceTypeIndex
    val esType = Config.esDeviceTypeType

    DeviceTypeStorage.getDocs(index, esType) map { res =>
      res.map(_.extract[DeviceType])
    }

  }

  /**
    * Create a [[DeviceType]].
    *
    * @param deviceType deviceType to persist
    * @return deviceType that was just created; None if it already existed
    */
  def create(deviceType: DeviceType): Future[Option[DeviceType]] = {

    // TODO integration tests
    // TODO implementation
    Future(Some(deviceType))

  }

  /**
    * Update a [[DeviceType]].
    *
    * @param deviceType deviceType to update
    * @return updated deviceType; None if something went wrong
    */
  def update(deviceType: DeviceType): Future[Option[DeviceType]] = {

    // TODO integration tests
    // TODO implementation
    Future(Some(deviceType))

  }

  /**
    * Check the database for device types and if none exist create the default types instead.
    *
    * @return deviceTypes currently in the database; never empty unless database is empty and the list of default deviceTypes is empty, too
    */
  def init(): Future[Seq[DeviceType]] = {

    // TODO integration tests
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
