package com.ubirch.avatar.core.device

import java.util.UUID

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.rest.device.{DeviceHistory, DeviceHistoryLegacy}
import com.ubirch.util.elasticsearch.util.SortBuilderUtil
import com.ubirch.util.elasticsearch.{EsBulkClient, EsSimpleClient}
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import org.elasticsearch.index.query.QueryBuilders
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2016-09-30
  */
object DeviceHistoryManager extends MyJsonProtocol
  with StrictLogging {

  private val index = Config.esDeviceDataHistoryIndex

  /**
    * Query the history of deviceDataHistory for a specified deviceId.
    *
    * @param deviceId id of the device for which we would like to get messages
    * @param from     paging parameter: skip the first x elements
    * @param size     paging parameter: return up to x elements
    * @return results ordered by "timestamp desc"; empty if no messages were found
    */
  def history(deviceId: String,
              from: Int = 0,
              size: Int = Config.esDefaultPageSize
             ): Future[Seq[DeviceHistory]] = {

    require(deviceId.nonEmpty, "deviceId may not be empty")

    val query = Some(QueryBuilders.termQuery("deviceId", deviceId))
    val sort = Some(SortBuilderUtil.sortBuilder("timestamp", asc = false))

    EsSimpleClient.getDocs(index, query, Some(from), Some(size), sort).map { res =>
      res.map(_.extract[DeviceHistoryLegacy]).map { ddpl =>
        upgradeDdl(ddpl)
      }
    }
  }

  /**
    * Search a device's history for all [[DeviceHistory]] within a certain time interval.
    *
    * @param deviceId device whose history we query
    * @param from     lower interval boundary (included in interval)
    * @param to       upper interval boundary (included in interval)
    * @return results ordered by "timestamp desc"
    */
  def byDate(deviceId: UUID, from: DateTime, to: DateTime): Future[Seq[DeviceHistory]] = {

    val combinedQuery = QueryBuilders.boolQuery()
      .must(QueryBuilders.termQuery("deviceId", deviceId.toString))
      .must(QueryBuilders.rangeQuery("timestamp").gte(from))
      .must(QueryBuilders.rangeQuery("timestamp").lte(to))

    val sort = Some(SortBuilderUtil.sortBuilder("timestamp", asc = false))

    EsSimpleClient.getDocs(
      docIndex = index,
      sort = sort,
      query = Some(combinedQuery),
      size = Some(Config.esLargePageSize)
    ) map { res =>
      res.map(_.extract[DeviceHistoryLegacy]).map { ddpl =>
        upgradeDdl(ddpl)
      }
    }

  }

  /**
    * Search a device's history for all [[DeviceHistory]] before a given timestamp.
    *
    * @param deviceId device whose history we query
    * @param before   search for messages before this timestamp
    * @return results ordered by "timestamp desc"
    */
  def before(deviceId: UUID, before: DateTime): Future[Seq[DeviceHistory]] = {

    val combinedQuery = QueryBuilders.boolQuery()
      .must(QueryBuilders.termQuery("deviceId", deviceId.toString))
      .must(QueryBuilders.rangeQuery("timestamp").lt(before))

    val sort = Some(SortBuilderUtil.sortBuilder("timestamp", asc = false))

    EsSimpleClient.getDocs(
      docIndex = index,
      sort = sort,
      query = Some(combinedQuery),
      size = Some(Config.esLargePageSize)
    ) map { res =>
      res.map(_.extract[DeviceHistoryLegacy]).map { ddpl =>
        upgradeDdl(ddpl)
      }
    }
  }

  /**
    * Search a device's history for all [[DeviceHistory]] after a given timestamp.
    *
    * @param deviceId device whose history we query
    * @param after    search for messages after this timestamp
    * @return results ordered by "timestamp desc"
    */
  def after(deviceId: UUID, after: DateTime): Future[Seq[DeviceHistory]] = {

    val combinedQuery = QueryBuilders.boolQuery()
      .must(QueryBuilders.termQuery("deviceId", deviceId.toString))
      .must(QueryBuilders.rangeQuery("timestamp").gt(after))

    val sort = Some(SortBuilderUtil.sortBuilder("timestamp", asc = false))

    EsSimpleClient.getDocs(
      docIndex = index,
      sort = sort,
      query = Some(combinedQuery),
      size = Some(Config.esLargePageSize)
    ) map { res =>
      res.map(_.extract[DeviceHistoryLegacy]).map { ddpl =>
        upgradeDdl(ddpl)
      }
    }

  }

  /**
    * Search a device's history for all [[DeviceHistory]] within a given day.
    *
    * @param deviceId device whose history we query
    * @param day      search for messages within this day
    * @return results ordered by "timestamp desc"
    */
  def byDay(deviceId: UUID, day: DateTime): Future[Seq[DeviceHistory]] = {

    logger.debug(s"search byDay: day=$day")
    val from = day.withHourOfDay(0)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val to = from.plusDays(1).minusMillis(1)
    logger.debug(s"search byDay: from=$from")
    logger.debug(s"search byDay: to=$to")

    byDate(deviceId, from, to)

  }

  /**
    * Query one raw data object
    *
    * @param messageId unique which identifies one raw data object
    * @return DeviceDataRaw or None
    */
  def history(messageId: UUID): Future[Option[DeviceHistory]] = {

    require(messageId != null, "raw data id may not be null")

    val query = Some(QueryBuilders.termQuery("messageId", messageId.toString))

    EsSimpleClient.getDocs(index, query).map { res =>
      res.map(_.extract[DeviceHistoryLegacy]).map { ddpl =>
        upgradeDdl(ddpl)
      }.headOption
    }
  }

  /**
    * Store a [[DeviceHistory]].
    *
    * @param data a device's processed data to store
    * @return json of what we stored
    */
  def store(data: DeviceHistory): Option[DeviceHistory] = {

    Json4sUtil.any2jvalue(data) match {

      case Some(doc) =>

        val id = data.messageId.toString
        logger.debug(s"store data ( /$index/$id ): $doc")
        logger.debug(s"jsonDoc: ${Json4sUtil.jvalue2String(doc)}")
        EsBulkClient.storeDocBulk(
          docIndex = index,
          docId = id,
          doc = doc
        ) match {
          case true =>
            logger.debug(s"stored doc for device ${data.deviceId}: $doc")
            doc.extractOpt[DeviceHistory]
          case false => None
        }

      case None =>
        logger.debug(s"failed storing device histoy for device: ${data.deviceId}")
        None
    }
  }


  private def upgradeDdl(ddpl: DeviceHistoryLegacy) = {
    val dn = if (ddpl.deviceName.isEmpty)
      s"${ddpl.deviceType}-${ddpl.deviceId}"
    else
      ddpl.deviceName.get.trim

    DeviceHistory(messageId = ddpl.messageId,
      deviceDataRawId = ddpl.deviceDataRawId,
      deviceId = ddpl.deviceId,
      deviceName = dn,
      deviceType = ddpl.deviceType,
      deviceTags = ddpl.deviceTags,
      deviceMessage = ddpl.deviceMessage,
      deviceDataRaw = ddpl.deviceDataRaw,
      timestamp = ddpl.timestamp
    )
  }

}
