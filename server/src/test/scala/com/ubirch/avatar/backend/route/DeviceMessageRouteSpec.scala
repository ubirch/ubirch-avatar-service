package com.ubirch.avatar.backend.route

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.core.server.util.RouteConstants
import com.ubirch.avatar.core.test.util.DeviceDataRawTestUtil
import com.ubirch.avatar.history.HistoryIndexUtil
import com.ubirch.avatar.model.DummyDeviceDataRaw
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.avatar.model.util.{ErrorFactory, ErrorResponse}
import com.ubirch.avatar.test.base.{ElasticsearchSpec, RouteSpec}

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-30
  */
class DeviceMessageRouteSpec extends RouteSpec
  with ElasticsearchSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.urlDeviceHistory(":deviceId")}") {

    scenario("deviceId exists; elementCount < defaultPageSize") {
      testGetHistoryDeviceExists(Config.esDefaultPageSize - 1, None, None)
    }

    scenario("deviceId exists; elementCount = defaultPageSize") {
      testGetHistoryDeviceExists(Config.esDefaultPageSize, None, None)
    }

    scenario("deviceId exists; elementCount > defaultPageSize") {
      testGetHistoryDeviceExists(Config.esDefaultPageSize + 1, None, None)
    }

    scenario("deviceId does not exists; Elasticsearch index does not exist either") {
      testGetHistoryDeviceExistsNot(None, None, indexExists = false)
    }

    scenario("deviceId does not exists; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(None, None, indexExists = true)
    }

  }

  feature(s"GET ${RouteConstants.urlDeviceHistory(":deviceId")}/:from") {

    scenario("deviceId exists; from < 0") {
      testGetHistoryDeviceExists(3, Some(-1), None)
    }

    scenario("deviceId exists; from = 0") {
      testGetHistoryDeviceExists(3, Some(0), None)
    }

    scenario("deviceId exists; from > 0") {
      testGetHistoryDeviceExists(3, Some(1), None)
    }

    scenario("deviceId does not exist; from < 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(-1), None, indexExists = false)
    }

    scenario("deviceId does not exist; from = 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(0), None, indexExists = false)
    }

    scenario("deviceId does not exist; from > 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(1), None, indexExists = false)
    }

    scenario("deviceId does not exist; from < 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(-1), None, indexExists = true)
    }

    scenario("deviceId does not exist; from = 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(0), None, indexExists = true)
    }

    scenario("deviceId does not exist; from > 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(1), None, indexExists = true)
    }

  }

  feature(s"GET ${RouteConstants.urlDeviceHistory(":deviceId")}/:from/:size") {

    scenario("deviceId exists; from < 0; size < 0") {
      testGetHistoryDeviceExists(1, Some(-1), Some(-1))
    }

    scenario("deviceId exists; from < 0; size = 0") {
      testGetHistoryDeviceExists(1, Some(-1), Some(0))
    }

    scenario("deviceId exists; from < 0; size > 0") {
      testGetHistoryDeviceExists(1, Some(-1), Some(10))
    }

    scenario("deviceId exists; from = 0; size < 0") {
      testGetHistoryDeviceExists(1, Some(0), Some(-1))
    }

    scenario("deviceId exists; from = 0; size < elementCount") {
      testGetHistoryDeviceExists(3, Some(0), Some(2))
    }

    scenario("deviceId exists; from = 0; size = elementCount") {
      testGetHistoryDeviceExists(3, Some(0), Some(3))
    }

    scenario("deviceId exists; from = 0; size > elementCount") {
      testGetHistoryDeviceExists(3, Some(0), Some(4))
    }

    scenario("deviceId exists; from > 0; size < elementCount") {
      testGetHistoryDeviceExists(3, Some(1), Some(2))
    }

    scenario("deviceId exists; from > 0; size = elementCount") {
      testGetHistoryDeviceExists(3, Some(1), Some(3))
    }

    scenario("deviceId exists; from > 0; size > elementCount") {
      testGetHistoryDeviceExists(3, Some(1), Some(4))
    }

    scenario("deviceId does not exist; from < 0; size < 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(-1), Some(-1), indexExists = false)
    }

    scenario("deviceId does not exist; from < 0; size = 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(-1), Some(0), indexExists = false)
    }

    scenario("deviceId does not exist; from < 0; size > 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(-1), Some(10), indexExists = false)
    }

    scenario("deviceId does not exist; from = 0; size < 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(0), Some(-1), indexExists = false)
    }

    scenario("deviceId does not exist; from = 0; size = 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(0), Some(0), indexExists = false)
    }

    scenario("deviceId does not exist; from = 0; size > 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(0), Some(10), indexExists = false)
    }

    scenario("deviceId does not exist; from > 0; size < 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(5), Some(-1), indexExists = false)
    }

    scenario("deviceId does not exist; from > 0; size = 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(5), Some(0), indexExists = false)
    }

    scenario("deviceId does not exist; from > 0; size > 0; Elasticsearch index does not exist") {
      testGetHistoryDeviceExistsNot(Some(5), Some(10), indexExists = false)
    }

    scenario("deviceId does not exist; from < 0; size < 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(-1), Some(-1), indexExists = true)
    }

    scenario("deviceId does not exist; from < 0; size = 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(-1), Some(0), indexExists = true)
    }

    scenario("deviceId does not exist; from < 0; size > 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(-1), Some(10), indexExists = true)
    }

    scenario("deviceId does not exist; from = 0; size < 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(0), Some(-1), indexExists = true)
    }

    scenario("deviceId does not exist; from = 0; size = 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(0), Some(0), indexExists = true)
    }

    scenario("deviceId does not exist; from = 0; size > 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(0), Some(10), indexExists = true)
    }

    scenario("deviceId does not exist; from > 0; size < 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(5), Some(-1), indexExists = true)
    }

    scenario("deviceId does not exist; from > 0; size = 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(5), Some(0), indexExists = true)
    }

    scenario("deviceId does not exist; from > 0; size > 0; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(Some(5), Some(10), indexExists = true)
    }

  }

  feature(s"POST ${RouteConstants.urlDeviceHistory}") {

    scenario("insert message (messageId does not exist yet)") {

      // prepare
      val deviceMsg = DummyDeviceDataRaw.data()

      // test
      Post(RouteConstants.urlDeviceHistory, deviceMsg) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual OK
        verifyCORSHeader()
        responseAs[DeviceDataRaw] shouldEqual deviceMsg

        Thread.sleep(1000)

        val deviceMsgList = Await.result(DeviceDataRawManager.history(deviceMsg.deviceId), 1 seconds)
        deviceMsgList.size should be(1)

        val deviceMsgInDb = deviceMsgList.head
        deviceMsgInDb should be(deviceMsg)

      }

    }

    scenario("update message (messageId already exists)") {

      // prepare
      val deviceMsg1 = DummyDeviceDataRaw.data()
      val deviceMsg2 = DummyDeviceDataRaw.data(deviceId = deviceMsg1.deviceId, messageId = deviceMsg1.messageId)

      // test
      Post(RouteConstants.urlDeviceHistory, deviceMsg2) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual OK
        verifyCORSHeader()
        responseAs[DeviceDataRaw] shouldEqual deviceMsg2

        Thread.sleep(1000)
        val deviceMsgList = Await.result(DeviceDataRawManager.history(deviceMsg2.deviceId), 1 seconds)
        deviceMsgList.size should be(1)

        val deviceMsgInDb = deviceMsgList.head
        deviceMsgInDb should be(deviceMsg2)

      }

    }

  }

  private def testGetHistoryDeviceExists(elementCount: Int, from: Option[Int], size: Option[Int]) = {

    // prepare
    val dataSeries: List[DeviceDataRaw] = DeviceDataRawTestUtil.storeSeries(elementCount).reverse
    val deviceId = dataSeries.head.deviceId
    val url = urlForTest(deviceId, from, size)

    // test
    Get(url) ~> Route.seal(routes) ~> check {

      // verify
      (from.isDefined && from.get < 0) || (size.isDefined && size.get < 0) match {

        case true => verifyMethodNotAllowed()

        case false =>

          status shouldEqual OK

          verifyCORSHeader()

          responseEntity.contentType should be(`application/json`)
          val resultSeq = responseAs[Seq[DeviceDataRaw]]

          val beginIndex = HistoryIndexUtil.calculateBeginIndex(from)
          val endIndexOpt = size match {
            case None => HistoryIndexUtil.calculateEndIndex(dataSeries.size, beginIndex)
            case Some(sizeValue) => HistoryIndexUtil.calculateEndIndex(dataSeries.size, beginIndex, sizeValue)
          }
          endIndexOpt match {

            case None => resultSeq should be('isEmpty)

            case Some(endIndex) =>
              val expectedSize = HistoryIndexUtil.calculateExpectedSize(beginIndex, endIndex)
              resultSeq.size shouldEqual expectedSize
              val dataSeriesSlice = dataSeries.slice(beginIndex, endIndex)
              for (i <- dataSeriesSlice.indices) {
                resultSeq(i) shouldEqual dataSeriesSlice(i)
              }

          }


      }

    }

  }

  private def testGetHistoryDeviceExistsNot(from: Option[Int],
                                            size: Option[Int],
                                            indexExists: Boolean
                                           ): Unit = {

    // prepare
    if (indexExists) {
      DeviceDataRawTestUtil.storeSeries(1)
    }
    val deviceId = "1234asdf"
    val url = urlForTest(deviceId, from, size)

    // test
    Get(url) ~> Route.seal(routes) ~> check {

      // verify
      (from.isDefined && from.get < 0) || (size.isDefined && size.get < 0) match {

        case true => verifyMethodNotAllowed()

        case false =>

          indexExists match {
            case true => verifyBadRequestDeviceNotFound(deviceId, from, size)
            case false => verifyInternalServerError()

          }

      }

    }

  }

  private def urlForTest(deviceId: String, fromOpt: Option[Int], sizeOpt: Option[Int]): String = {

    fromOpt match {

      case Some(from) =>
        sizeOpt match {
          case Some(sizeValue) => RouteConstants.urlDeviceHistoryFromSize(deviceId, from, sizeValue)
          case None => RouteConstants.urlDeviceHistoryFrom(deviceId, from)
        }

      case None => RouteConstants.urlDeviceHistory(deviceId)

    }

  }

  private def verifyMethodNotAllowed(): Unit = {
    status shouldEqual MethodNotAllowed
    verifyCORSHeader(exist = false)
  }

  private def verifyInternalServerError(): Unit = {
    status shouldEqual InternalServerError
    verifyCORSHeader(exist = false)
  }

  private def verifyBadRequestDeviceNotFound(deviceId: String, from: Option[Int], size: Option[Int]): Unit = {

    status shouldEqual BadRequest

    val expectedError = ErrorFactory.create("QueryError", s"deviceId not found: deviceId=$deviceId, from=$from, size=$size")
    responseEntity.contentType should be(`application/json`)
    responseAs[ErrorResponse] shouldEqual expectedError

    verifyCORSHeader()

  }

}
