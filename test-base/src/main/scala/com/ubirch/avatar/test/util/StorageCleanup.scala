package com.ubirch.avatar.test.util

import java.net.URL

import com.ubirch.avatar.config.Config

import uk.co.bigbeeconsultants.http.HttpClient

/**
  * author: cvandrei
  * since: 2016-10-26
  */
trait StorageCleanup {

  private val indexInfos: Seq[IndexInfo] = Seq(IndexInfo(Config.esHost, Config.esPortHttp, Config.esDeviceHistoryIndex))

  /**
    * Delete all indexes.
    */
  final def resetStorage(): Unit = {

    val httpClient = new HttpClient
    indexInfos foreach { indexTuple =>
      httpClient.delete(indexTuple.url)
    }

  }

}

case class IndexInfo(host: String, port: Int, index: String) {
  def url: URL = new URL(s"http://$host:$port/$index")
}