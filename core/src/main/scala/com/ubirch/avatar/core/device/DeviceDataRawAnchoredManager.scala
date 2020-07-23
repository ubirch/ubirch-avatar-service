package com.ubirch.avatar.core.device

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.util.elasticsearch.{EsBulkClient, EsSimpleClient}
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-02-22
  */
object DeviceDataRawAnchoredManager extends MyJsonProtocol
  with StrictLogging {

  private val index = Config.esDeviceDataRawAnchoredIndex

  /**
    * Search an anchored [[DeviceDataRaw]] (with txHash) based on it's id.
    *
    * @param id id to search with
    * @return None if nothing was found
    */
  def byId(id: UUID): Future[Option[DeviceDataRaw]] = {

    logger.debug(s"query byId: id=$id")

    EsSimpleClient.getDoc(
      docIndex = index,
      docId = id.toString
    ) map {
      case Some(res) => Some(res.extract[DeviceDataRaw])
      case None => None
    }

  }

  /**
    * Store an anchored [[DeviceDataRaw]] (field "txHash" is set).
    *
    * @param data a device's raw data to store
    * @return json of what we stored
    */
  def store(data: DeviceDataRaw): Option[DeviceDataRaw] = {

    logger.debug(s"store data: $data")
    Json4sUtil.any2jvalue(data) match {

      case Some(doc) =>

        val id = data.id.toString
        EsBulkClient.storeDocBulk(
          docIndex = index,
          docId = id,
          doc = doc
        )
        Some(data)

      case None => None

    }

  }

}
