package com.ubirch.avatar.core.device

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.services.storage.DeviceDataRawAnchoredBulkStorage
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-02-22
  */
object DeviceDataRawAnchoredManager extends MyJsonProtocol
  with StrictLogging {

  /**
    * Store an anchored [[DeviceDataRaw]] (field "txHash" is set).
    *
    * @param data a device's raw data to store
    * @return json of what we stored
    */
  def store(data: DeviceDataRaw): Future[Option[DeviceDataRaw]] = {

    logger.debug(s"store data: $data")
    Json4sUtil.any2jvalue(data) match {

      case Some(doc) =>

        val index = Config.esDeviceDataRawAnchoredIndex
        val esType = Config.esDeviceDataRawAnchoredType

        val id = data.id.toString
        DeviceDataRawAnchoredBulkStorage.storeDocBulk(
          docIndex = index,
          docType = esType,
          docId = id,
          doc = doc,
          timestamp = DateTime.now.getMillis
        ) map (_.extractOpt[DeviceDataRaw])

      case None => Future(None)

    }

  }

}
