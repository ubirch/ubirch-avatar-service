package com.ubirch.services.storage

import java.net.InetAddress

import com.typesafe.scalalogging.LazyLogging
import com.ubirch.avatar.config.Config
import com.ubirch.util.json.Json4sUtil
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.json4s.JValue

/**
  * Created by derMicha on 06/10/16.
  */
object ElasticsearchStorage extends LazyLogging {

  private val esclient: TransportClient = TransportClient.builder().build()
    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(Config.esHost), Config.esPort))

  /**
    *
    * @param docIndex
    * @param docType
    * @param docId
    * @param doc
    * @return
    */
  def storeDoc(docIndex: String, docType: String, docId: String, doc: JValue): Option[JValue] = storeDoc(docIndex, docType, docId, 0l, doc)

  /**
    *
    * @param docIndex
    * @param docType
    * @param docId
    * @param ttl
    * @param doc
    * @return
    */
  def storeDoc(docIndex: String, docType: String, docId: String, ttl: Long, doc: JValue): Option[JValue] = {
    if (docIndex.isEmpty || docType.isEmpty || docId.isEmpty) {
      None
    }
    else {
      Json4sUtil.jvalue2String(doc) match {
        case docStr if docStr.nonEmpty =>
          val pIdx = esclient.prepareIndex(docIndex, docType, docId)
            .setSource(docStr)
          if (ttl > 0) {
            pIdx.setTTL(ttl)
          }
          val res = pIdx.get()
          if (res.getId == docId)
            Some(doc)
          else
            None
        case _ =>
          None
      }
    }
  }

  /**
    *
    * @param docIndex name of the ElasticSeatch index
    * @param docType  name of the type of document
    * @param docId    unique Id per Document
    * @return
    */
  def getDoc(docIndex: String, docType: String, docId: String): Option[JValue] = {
    if (docIndex.isEmpty || docType.isEmpty || docId.isEmpty) {
      None
    }
    else {
      esclient.prepareGet(docIndex, docType, docId).get() match {
        case rs if rs.isExists =>
          Json4sUtil.string2JValue(rs.getSourceAsString)
        case _ =>
          logger.error(s"doc could not be found: $docIndex / $docType /$docId")
          None
      }
    }
  }

  /**
    * removes a document from it's index
    * @param docIndex name of the index
    * @param docType name of the doc type
    * @param docId unique id
    * @return
    */
  def deleteDoc(docIndex: String, docType: String, docId: String): Boolean = {
    if (docIndex.isEmpty || docType.isEmpty || docId.isEmpty) {
      false
    }
    else {
      val res = esclient.prepareDelete(docIndex, docType, docId).get()
      res.isFound
    }
  }
}
