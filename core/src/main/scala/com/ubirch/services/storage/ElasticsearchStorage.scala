package com.ubirch.services.storage

import java.net.InetAddress

import com.typesafe.scalalogging.slf4j.LazyLogging
import com.ubirch.avatar.config.Config
import com.ubirch.util.json.Json4sUtil
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.json4s.{DefaultFormats, JValue}

import scala.concurrent.{Future, Promise}

/**
  * Created by derMicha on 06/10/16.
  */
object ElasticsearchStorage extends LazyLogging {

  implicit val formats = DefaultFormats.lossless ++ org.json4s.ext.JodaTimeSerializers.all
  implicit val ec = scala.concurrent.ExecutionContext.global

  private val prom = Promise[String]()

  private val esclient: TransportClient = TransportClient.builder().build()
    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(Config.esHost), Config.esPort))

  /**
    *
    * @param docIndex name of the index into which the current document should be stored
    * @param docType  name of the current documents type
    * @param docId    unique id which identifies current document uniquely inside the index
    * @param doc      document as a JValue which should be stored
    * @return
    */
  def storeDoc(docIndex: String, docType: String, docId: String, doc: JValue): Future[JValue] = storeDoc(docIndex, docType, docId, 0l, doc)

  /**
    *
    * @param docIndex name of the index into which the current document should be stored
    * @param docType  name of the current documents type
    * @param docId    unique id which identifies current document uniquely inside the index
    * @param ttl      sets the relative ttl value in milliseconds, a vlaue of 0 means no ttl
    * @param doc      document as a JValue which should be stored
    * @return
    */
  def storeDoc(docIndex: String, docType: String, docId: String, ttl: Long, doc: JValue): Future[JValue] = Future {
    if (docIndex.isEmpty || docType.isEmpty || docId.isEmpty) {
      //      Future.failed(new Exception("json invalid arguments"))
      throw new Exception("json invalid arguments")
    }
    else Json4sUtil.jvalue2String(doc) match {
      case docStr if docStr.nonEmpty =>
        val pIdx = esclient.prepareIndex(docIndex, docType, docId)
          .setSource(docStr)
        if (ttl > 0) {
          pIdx.setTTL(ttl)
        }

        val res = pIdx.get()
        if (res.getId == docId)
          doc
        else
          throw new Exception("store failed")


      case _ =>
        throw new Exception("json failed")
    }
  }

  /**
    *
    * @param docIndex name of the ElasticSeatch index
    * @param docType  name of the type of document
    * @param docId    unique Id per Document
    * @return
    */
  def getDoc(docIndex: String, docType: String, docId: String): Future[Option[JValue]] = Future {
    if (docIndex.isEmpty || docType.isEmpty || docId.isEmpty) {
      throw new Exception("json invalid arguments")
    }
    else {
      esclient.prepareGet(docIndex, docType, docId).get() match {
        case rs if rs.isExists =>
          Json4sUtil.string2JValue(rs.getSourceAsString)
        case _ =>
          //          logger.error(s"doc could not be found: $docIndex / $docType /$docId")
          None
      }
    }
  }

  /**
    *
    * @param docIndex name of the ElasticSeatch index
    * @param docType  name of the type of document
    * @return
    */
  def getDocs(docIndex: String, docType: String): Future[List[JValue]] = {
    if (docIndex.isEmpty || docType.isEmpty) {
      Future.failed(new Exception("json invalid arguments"))
    }
    else {
      Future {
        esclient.prepareSearch(docIndex)
          .setTypes(docType)
          .execute()
          .get() match {
          case srs if srs.getHits.getTotalHits > 0 =>
            srs.getHits.getHits.map { hit =>
              Json4sUtil.string2JValue(hit.getSourceAsString)
            }.filter(_.isDefined).map(_.get.extract[JValue]).toList
          case _ =>
            List()
        }
      }
    }
  }

  /**
    * removes a document from it's index
    *
    * @param docIndex name of the index
    * @param docType  name of the doc type
    * @param docId    unique id
    * @return
    */
  def deleteDoc(docIndex: String, docType: String, docId: String): Future[Boolean] = Future {
    if (docIndex.isEmpty || docType.isEmpty || docId.isEmpty) {
      throw new Exception("json invalid arguments")
    }
    else {
      val res = esclient.prepareDelete(docIndex, docType, docId).get()
      res.isFound
    }
  }
}
