package com.ubirch.services.storage

import java.net.InetAddress

import com.typesafe.scalalogging.slf4j.LazyLogging

import com.ubirch.avatar.config.Config
import com.ubirch.util.json.Json4sUtil

import org.elasticsearch.action.bulk.{BackoffPolicy, BulkProcessor, BulkRequest, BulkResponse}
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.unit.{ByteSizeUnit, ByteSizeValue, TimeValue}
import org.json4s.JValue

/**
  * Created by derMicha on 02/10/16.
  */
object ElasticsearchBulkStorage extends LazyLogging {

  private val esClient: TransportClient = TransportClient.builder().build()
    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(Config.esHost), Config.esPort))

  private val bulkProcessor = BulkProcessor.builder(esClient, new BulkProcessor.Listener() {

    @Override
    def beforeBulk(executionId: Long, request: BulkRequest): Unit = {
      logger.info("beforeBulk")
    }

    @Override
    def afterBulk(executionId: Long, request: BulkRequest, response: BulkResponse): Unit = {
      logger.info("afterBulk")
    }

    @Override
    def afterBulk(executionId: Long, request: BulkRequest, failure: Throwable): Unit = {
      logger.error("afterBulk", failure)
    }
  }
  )
    .setBulkActions(2000)
    .setBulkSize(new ByteSizeValue(10, ByteSizeUnit.MB))
    .setFlushInterval(TimeValue.timeValueSeconds(5))
    .setConcurrentRequests(2)
    .setBackoffPolicy(
      BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
    .build()

  def storeBulkData(index: String, datatype: String, primaryKey: String, data: JValue, timestamp: Long): JValue = {

    //    val ts = new DateTime(timestamp).toString(ISODateTimeFormat.basicDateTime())
    //    val ts = new DateTime(timestamp).toString(new DateTimeFormatter().withLocale(Locale.ENGLISH))
    //    val fixedJvalue = data merge render("created" -> ts)
    bulkProcessor.add(
      new IndexRequest(index, datatype, primaryKey)
        .source(Json4sUtil.jvalue2String(data))
        .timestamp(timestamp.toString)
    )
    data
  }

  def closeConnection(): Unit = {
    bulkProcessor.close()
  }
}