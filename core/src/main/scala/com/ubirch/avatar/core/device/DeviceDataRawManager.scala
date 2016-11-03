package com.ubirch.avatar.core.device

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.{Device, DeviceDataRaw}
import com.ubirch.services.storage.DeviceDataRawStorage
import com.ubirch.util.elasticsearch.client.util.SortUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import org.elasticsearch.index.query.QueryBuilders

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionException, Future}

/**
  * author: cvandrei
  * since: 2016-09-30
  */
object DeviceDataRawManager extends MyJsonProtocol {

  /**
    * Query the history of deviceDataRaw for a specified device.
    *
    * @param device device for which we would like to get raw data
    * @param from   paging parameter: skip the first x elements
    * @param size   paging parameter: return up to x elements
    * @return result list; empty if no messages were found
    * @throws ExecutionException       something went wrong (e.g. no document matching our query exists yet)
    * @throws IllegalArgumentException device.hwDeviceId is empty
    */
  def history(device: Device,
              from: Int = 0,
              size: Int = Config.esDefaultPageSize
             ): Future[Seq[DeviceDataRaw]] = {

    require(device.hwDeviceId.nonEmpty, "hwDeviceId may not be empty")

    val index = Config.esDeviceDataRawIndex
    val esType = Config.esDeviceDataRawType
    val query = Some(QueryBuilders.termQuery("a", device.hwDeviceId))
    val sort = Some(SortUtil.sortBuilder("timestamp", asc = false))

    DeviceDataRawStorage.getDocs(index, esType, query, Some(from), Some(size), sort).map { res =>
      res.map { jv =>
        jv.extract[DeviceDataRaw]
      }
    }

  }

  /**
    * Store a [[DeviceDataRaw]].
    *
    * @param data a device's raw data to store (messageId will be ignored)
    * @return json of what we stored
    */
  def store(data: DeviceDataRaw): Future[Option[DeviceDataRaw]] = {

    Json4sUtil.any2jvalue(data) match {

      case Some(doc) =>
        val index = Config.esDeviceDataRawIndex
        val esType = Config.esDeviceDataRawType
        val id = None
        DeviceDataRawStorage.storeDoc(index, esType, id, doc) map { jv =>
          Some(jv.extract[DeviceDataRaw])
        }

      case None => Future(None)

    }

  }

}
