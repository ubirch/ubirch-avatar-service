package com.ubirch.avatar.core.device

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.DeviceMessage
import com.ubirch.services.storage.DeviceDataStorage
import com.ubirch.util.elasticsearch.client.util.SortUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import org.elasticsearch.index.query.QueryBuilders

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-30
  */
object DeviceMessageManager extends MyJsonProtocol {

  def history(deviceId: String,
              from: Int = 0,
              size: Int = Config.esDefaultPageSize
             ): Future[Seq[DeviceMessage]] = {

    require(deviceId.nonEmpty, "deviceId may not be empty")

    val index = Config.esDeviceDataIndex
    val esType = Config.esDeviceDataType
    val query = Some(QueryBuilders.termQuery("deviceId", deviceId))
    val sort = Some(SortUtil.sortBuilder("timestamp", asc = false))

    DeviceDataStorage.getDocs(index, esType, query, Some(from), Some(size), sort).map { res =>
      res.map { jv =>
        jv.extract[DeviceMessage]
      }
    }

  }

  def store(data: DeviceMessage): Future[Option[DeviceMessage]] = {

    Json4sUtil.any2jvalue(data) match {

      case Some(doc) =>
        val index = Config.esDeviceDataIndex
        val esType = Config.esDeviceDataType
        val id = Some(data.messageId)
        DeviceDataStorage.storeDoc(index, esType, id, doc) map { jv =>
          Some(jv.extract[DeviceMessage])
        }

      case None => Future(None)

    }

  }

}
