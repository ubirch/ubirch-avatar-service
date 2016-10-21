package com.ubirch.services.storage

import java.util.concurrent.ExecutionException

import com.typesafe.scalalogging.slf4j.LazyLogging

import com.ubirch.util.json.Json4sUtil

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.index.IndexNotFoundException
import org.elasticsearch.index.query.QueryBuilder
import org.json4s.{DefaultFormats, JValue}

import scala.Predef._
import scala.concurrent.Future

/**
  * Using the Elasticsearch TransportClient to access the database: https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html
  *
  * Created by derMicha on 06/10/16.
  */
// TODO extract to ubirch-scala-utils/elasticsearch-binary-client project
trait ElasticsearchStorage extends LazyLogging {

  implicit val formats = DefaultFormats.lossless ++ org.json4s.ext.JodaTimeSerializers.all
  implicit val ec = scala.concurrent.ExecutionContext.global

  protected val esClient: TransportClient

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
    * @param ttl      sets the relative ttl value in milliseconds, a value of 0 means no ttl
    * @param doc      document as a JValue which should be stored
    * @return
    */
  def storeDoc(docIndex: String, docType: String, docId: String, ttl: Long, doc: JValue): Future[JValue] = Future {

    require(docIndex.nonEmpty && docType.nonEmpty && docId.nonEmpty, "json invalid arguments")

    Json4sUtil.jvalue2String(doc) match {
      case docStr if docStr.nonEmpty =>
        val pIdx = esClient.prepareIndex(docIndex, docType, docId)
          .setSource(docStr)
        if (ttl > 0) {
          pIdx.setTTL(ttl)
        }

        val res = pIdx.get()

        res.getId == docId match {
          case true => doc
          case _ => throw new Exception("store failed")
        }

      case _ => throw new Exception("json failed")
    }

  }

  /**
    *
    * @param docIndex name of the ElasticSearch index
    * @param docType  name of the type of document
    * @param docId    unique Id per Document
    * @return
    */
  def getDoc(docIndex: String, docType: String, docId: String): Future[Option[JValue]] = Future {

    require(docIndex.nonEmpty && docType.nonEmpty && docId.nonEmpty, "json invalid arguments")

    esClient.prepareGet(docIndex, docType, docId).get() match {
      case rs if rs.isExists => Json4sUtil.string2JValue(rs.getSourceAsString)
      case _ => None
    }

  }

  /**
    * @param docIndex name of the ElasticSearch index
    * @param docType  name of the type of document
    * @param query    search query as created with [[org.elasticsearch.index.query.QueryBuilders]]
    * @param from     pagination from
    * @param size     maximum number of results
    * @return
    */
  def getDocs(docIndex: String,
              docType: String,
              query: Option[QueryBuilder] = None,
              from: Option[Int] = None,
              size: Option[Int] = None
             ): Future[List[JValue]] = {

    require(docIndex.nonEmpty && docType.nonEmpty, "json invalid arguments")

    Future {
      var requestBuilder = esClient.prepareSearch(docIndex)
        .setTypes(docType)

      if (query.isDefined) {
        requestBuilder = requestBuilder.setQuery(query.get)
      }

      if (from.isDefined) {
        requestBuilder = requestBuilder.setFrom(from.get)
      }

      if (size.isDefined) {
        requestBuilder = requestBuilder.setSize(size.get)
      }

      try {

        requestBuilder.execute()
          .get() match {

          case srs if srs.getHits.getTotalHits > 0 =>
            srs.getHits.getHits.map { hit =>
              Json4sUtil.string2JValue(hit.getSourceAsString)
            }.filter(_.isDefined).map(_.get.extract[JValue]).toList
          case _ =>
            List()
        }

      } catch {
        case execExc: ExecutionException if execExc.getCause.getCause.isInstanceOf[IndexNotFoundException] => List()
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

    require(docIndex.nonEmpty && docType.nonEmpty && docId.nonEmpty, "json invalid arguments")

    val res = esClient.prepareDelete(docIndex, docType, docId).get()
    res.isFound

  }

}
