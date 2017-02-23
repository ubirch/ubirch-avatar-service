package com.ubirch.services.storage

import java.net.InetAddress

import com.ubirch.avatar.config.Config
import com.ubirch.util.elasticsearch.client.binary.{ElasticsearchBulkStorage, ElasticsearchStorage}
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress

/**
  * author: cvandrei
  * since: 2017-02-22
  */
trait BulkStorage extends ElasticsearchBulkStorage {

  private val address = new InetSocketTransportAddress(InetAddress.getByName(Config.esHost), Config.esPortBinary)

  private val settings: Settings = Settings.builder()
    .put("cluster.name", Config.esClusterName).build()

  override protected val esClient: TransportClient = TransportClient.builder()
    .settings(settings)
    .build()
    .addTransportAddress(address)

}

trait SimpleStorage extends ElasticsearchStorage {

  private val address = new InetSocketTransportAddress(InetAddress.getByName(Config.esHost), Config.esPortBinary)

  private val settings: Settings = Settings.builder()
    .put("cluster.name", Config.esClusterName).build()

  override protected val esClient: TransportClient = TransportClient.builder()
    .settings(settings)
    .build()
    .addTransportAddress(address)

}
