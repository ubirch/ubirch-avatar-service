package com.ubirch.avatar.core.device

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.DeviceData
import com.ubirch.services.storage.DeviceDataStorage
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import org.elasticsearch.index.query.QueryBuilders

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-30
  */
object DeviceDataManager extends MyJsonProtocol {

  def history(deviceId: String,
              from: Int = 0,
              size: Int = Config.deviceDataDbDefaultPageSize
             ): Future[Seq[DeviceData]] = {

    require(deviceId.nonEmpty, "deviceId may not be empty")

    val index = Config.deviceDataDbIndex
    val esType = Config.deviceDataDbType
    val query = Some(QueryBuilders.termQuery("deviceId", deviceId))

    DeviceDataStorage.getDocs(index, esType, query, Some(from), Some(size)).map { res =>
      res.map { jv =>
        jv.extract[DeviceData]
      }
    }

  }

  def store(data: DeviceData): Future[Option[DeviceData]] = {

    Json4sUtil.any2jvalue(data) match {

      case Some(doc) =>
        val index = Config.deviceDataDbIndex
        val esType = Config.deviceDataDbType
        val id = Some(data.messageId)
        DeviceDataStorage.storeDoc(index, esType, id, doc) map { jv =>
          Some(jv.extract[DeviceData])
        }

      case None => Future(None)

    }

  }

}
