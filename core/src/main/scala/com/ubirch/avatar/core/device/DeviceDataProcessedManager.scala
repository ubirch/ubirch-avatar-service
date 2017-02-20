package com.ubirch.avatar.core.device

import java.util.UUID

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.device.DeviceDataProcessed
import com.ubirch.services.storage.DeviceDataProcessedStorage
import com.ubirch.util.elasticsearch.client.util.SortUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import org.elasticsearch.index.query.QueryBuilders
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-30
  */
object DeviceDataProcessedManager extends MyJsonProtocol {

  private val index = Config.esDeviceDataProcessedIndex
  private val esType = Config.esDeviceDataProcessedType

  /**
    * Query the history of deviceDataHistory for a specified deviceId.
    *
    * @param deviceId id of the device for which we would like to get messages
    * @param from     paging parameter: skip the first x elements
    * @param size     paging parameter: return up to x elements
    * @return result list; empty if no messages were found
    */
  def history(deviceId: String,
              from: Int = 0,
              size: Int = Config.esDefaultPageSize
             ): Future[Seq[DeviceDataProcessed]] = {

    require(deviceId.nonEmpty, "deviceId may not be empty")

    val query = Some(QueryBuilders.termQuery("deviceId", deviceId))
    val sort = Some(SortUtil.sortBuilder("timestamp", asc = false))

    DeviceDataProcessedStorage.getDocs(index, esType, query, Some(from), Some(size), sort).map { res =>
      res.map(_.extract[DeviceDataProcessed])
    }

  }

  /**
    * Search a device's history for all [[DeviceDataProcessed]] within a certain time interval.
    *
    * @param deviceId device whose history we query
    * @param from     lower interval boundary (included in interval)
    * @param to       upper interval boundary (included in interval)
    * @return results ordered by "timestamp asc"
    */
  def byDate(deviceId: UUID, from: DateTime, to: DateTime): Future[Seq[DeviceDataProcessed]] = {

    // TODO automated tests
    val combinedQuery = QueryBuilders.boolQuery()
      .must(QueryBuilders.termQuery("deviceId", deviceId.toString))
      .must(QueryBuilders.rangeQuery("timestamp").gte(from))
      .must(QueryBuilders.rangeQuery("timestamp").lte(to))

    val sort = Some(SortUtil.sortBuilder("timestamp", asc = true))

    DeviceDataProcessedStorage.getDocs(
      docIndex = index,
      docType = esType,
      sort = sort,
      query = Some(combinedQuery)
    ) map { res =>
      res.map(_.extract[DeviceDataProcessed])
    }

  }

  /**
    * Search a device's history for all [[DeviceDataProcessed]] before a given timestamp.
    *
    * @param deviceId device whose history we query
    * @param before   search for messages before this timestamp
    * @return results ordered by "timestamp asc"
    */
  def before(deviceId: UUID, before: DateTime): Future[Seq[DeviceDataProcessed]] = {

    // TODO automated tests
    val combinedQuery = QueryBuilders.boolQuery()
      .must(QueryBuilders.termQuery("deviceId", deviceId.toString))
      .must(QueryBuilders.rangeQuery("timestamp").lt(before))

    val sort = Some(SortUtil.sortBuilder("timestamp", asc = true))

    DeviceDataProcessedStorage.getDocs(
      docIndex = index,
      docType = esType,
      sort = sort,
      query = Some(combinedQuery)
    ) map { res =>
      res.map(_.extract[DeviceDataProcessed])
    }

  }

  /**
    * Search a device's history for all [[DeviceDataProcessed]] after a given timestamp.
    *
    * @param deviceId device whose history we query
    * @param after    search for messages after this timestamp
    * @return results ordered by "timestamp asc"
    */
  def after(deviceId: UUID, after: DateTime): Future[Seq[DeviceDataProcessed]] = {

    // TODO automated tests
    val combinedQuery = QueryBuilders.boolQuery()
      .must(QueryBuilders.termQuery("deviceId", deviceId.toString))
      .must(QueryBuilders.rangeQuery("timestamp").gt(after))

    val sort = Some(SortUtil.sortBuilder("timestamp", asc = true))

    DeviceDataProcessedStorage.getDocs(
      docIndex = index,
      docType = esType,
      sort = sort,
      query = Some(combinedQuery)
    ) map { res =>
      res.map(_.extract[DeviceDataProcessed])
    }

  }

  /**
    * Search a device's history for all [[DeviceDataProcessed]] within a given day.
    *
    * @param deviceId device whose history we query
    * @param day      search for messages within this day
    * @return results ordered by "timestamp asc"
    */
  def byDay(deviceId: UUID, day: DateTime): Future[Seq[DeviceDataProcessed]] = {

    // TODO automated tests
    val from = day.withHourOfDay(0)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val to = from.plusDays(1).minusMillis(1)

    byDate(deviceId, from, to)

  }

  /**
    * Query one raw data object
    *
    * @param messageId unique which identifies one raw data object
    * @return DeviceDataRaw or None
    */
  def history(messageId: UUID): Future[Option[DeviceDataProcessed]] = {

    require(messageId != null, "raw data id may not be null")

    val query = Some(QueryBuilders.termQuery("messageId", messageId.toString))

    DeviceDataProcessedStorage.getDocs(index, esType, query).map { res =>
      res.map(_.extract[DeviceDataProcessed]).headOption
    }
  }

  /**
    * Store a [[DeviceDataProcessed]].
    *
    * @param data a device's processed data to store
    * @return json of what we stored
    */
  def store(data: DeviceDataProcessed): Future[Option[DeviceDataProcessed]] = {

    Json4sUtil.any2jvalue(data) match {

      case Some(doc) =>
        val id = Some(data.messageId.toString)
        DeviceDataProcessedStorage.storeDoc(
          docIndex = index,
          docType = esType,
          docIdOpt = id,
          doc = doc
        ) map (_.extractOpt[DeviceDataProcessed])

      case None => Future(None)

    }

  }

}
