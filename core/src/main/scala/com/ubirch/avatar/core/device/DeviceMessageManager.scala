package com.ubirch.avatar.core.device

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.DeviceMessage
import com.ubirch.services.storage.DeviceDataStorage
import com.ubirch.util.elasticsearch.client.util.SortUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import org.elasticsearch.index.query.QueryBuilders

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionException, Future}

/**
  * author: cvandrei
  * since: 2016-09-30
  */
object DeviceMessageManager extends MyJsonProtocol {

  /**
    * Query the history of deviceMessages for a specified deviceId.
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
             ): Future[Seq[DeviceMessage]] = {

    require(deviceId.nonEmpty, "deviceId may not be empty")

    val index = Config.esDeviceHistoryIndex
    val esType = Config.esDeviceHistoryType
    val query = Some(QueryBuilders.termQuery("deviceId", deviceId))
    val sort = Some(SortUtil.sortBuilder("timestamp", asc = false))

    DeviceDataStorage.getDocs(index, esType, query, Some(from), Some(size), sort).map { res =>
      res.map { jv =>
        jv.extract[DeviceMessage]
      }
    }

  }

  /**
    * Store a [[DeviceMessage]].
    *
    * @param data device message to store
    * @return json of what we stored
    */
  def store(data: DeviceMessage): Future[Option[DeviceMessage]] = {

    Json4sUtil.any2jvalue(data) match {

      case Some(doc) =>
        val index = Config.esDeviceHistoryIndex
        val esType = Config.esDeviceHistoryType
        val id = Some(data.messageId)
        DeviceDataStorage.storeDoc(index, esType, id, doc) map { jv =>
          Some(jv.extract[DeviceMessage])
        }

      case None => Future(None)

    }

  }

}
