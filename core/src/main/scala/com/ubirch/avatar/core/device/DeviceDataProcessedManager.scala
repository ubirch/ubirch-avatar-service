package com.ubirch.avatar.core.device

import java.util.UUID

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.{DeviceDataProcessed, DeviceDataRaw}
import com.ubirch.services.storage.{DeviceDataProcessedStorage, DeviceDataRawStorage}
import com.ubirch.util.elasticsearch.client.util.SortUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.uuid.UUIDUtil
import org.elasticsearch.index.query.QueryBuilders

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionException, Future}

/**
  * author: cvandrei
  * since: 2016-09-30
  */
object DeviceDataProcessedManager extends MyJsonProtocol {

  /**
    * Query the history of deviceDataHistory for a specified deviceId.
    *
    * @param deviceId id of the device for which we would like to get messages
    * @param from     paging parameter: skip the first x elements
    * @param size     paging parameter: return up to x elements
    * @return result list; empty if no messages were found
    * @throws ExecutionException something went wrong (e.g. no document matching our query exists yet)
    */
  def history(deviceId: String,
              from: Int = 0,
              size: Int = Config.esDefaultPageSize
             ): Future[Seq[DeviceDataProcessed]] = {

    require(deviceId.nonEmpty, "deviceId may not be empty")

    val index = Config.esDeviceDataProcessedIndex
    val esType = Config.esDeviceDataProcessedType
    val query = Some(QueryBuilders.termQuery("deviceId", deviceId))
    val sort = Some(SortUtil.sortBuilder("timestamp", asc = false))

    DeviceDataProcessedStorage.getDocs(index, esType, query, Some(from), Some(size), sort).map { res =>
      res.map { jv =>
        jv.extract[DeviceDataProcessed]
      }
    }

  }

  /**
    * Query one raw data object
    *
    * @param messageId unique which identifies one raw data object
    * @return DeviceDataRaw or None
    */
  def history(messageId: UUID): Future[Option[DeviceDataProcessed]] = {

    require(messageId != null, "raw data id may not be null")

    val index = Config.esDeviceDataProcessedIndex
    val esType = Config.esDeviceDataProcessedType
    val query = Some(QueryBuilders.termQuery("messageId", messageId.toString))

    DeviceDataRawStorage.getDocs(index, esType, query).map { res =>
      res.map { jv =>
        jv.extract[DeviceDataProcessed]
      }.headOption
    }
  }

  /**
    * Store a [[DeviceDataProcessed]].
    *
    * @param data a device's processed data to store (messageId will be ignored)
    * @return json of what we stored
    */
  def store(data: DeviceDataProcessed): Future[Option[DeviceDataProcessed]] = {

    val toStore = data.copy(messageId = UUIDUtil.uuid)
    Json4sUtil.any2jvalue(toStore) match {

      case Some(doc) =>
        val index = Config.esDeviceDataProcessedIndex
        val esType = Config.esDeviceDataProcessedType
        val id = Some(toStore.messageId.toString)
        DeviceDataProcessedStorage.storeDoc(
          docIndex = index,
          docType = esType,
          docIdOpt = id,
          doc = doc
        ) map { jv =>
          Some(jv.extract[DeviceDataProcessed])
        }

      case None => Future(None)

    }

  }

}
