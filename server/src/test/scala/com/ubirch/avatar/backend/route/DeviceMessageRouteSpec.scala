package com.ubirch.avatar.backend.route

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.server.util.RouteConstants
import com.ubirch.avatar.core.test.util.DeviceMessageTestUtil
import com.ubirch.avatar.model.device.DeviceMessage
import com.ubirch.avatar.model.util.{ErrorFactory, ErrorResponse}
import com.ubirch.avatar.test.base.{ElasticsearchSpec, RouteSpec}
import com.ubirch.util.uuid.UUIDUtil

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-30
  */
class DeviceMessageRouteSpec extends RouteSpec
  with ElasticsearchSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.urlDeviceHistory(":deviceId")}") {

    scenario("deviceId exists") {

      // prepare
      val elementCount = Config.esDefaultPageSize
      val dataSeries: List[DeviceMessage] = DeviceMessageTestUtil.storeSeries(elementCount).reverse
      val deviceId = dataSeries.head.deviceId

      // test
      Get(RouteConstants.urlDeviceHistory(deviceId)) ~> routes ~> check {

        // verify
        status shouldEqual OK

        responseEntity.contentType should be(`application/json`)
        val resultSeq = responseAs[Seq[DeviceMessage]]
        resultSeq.size should be(elementCount)
        for (i <- 0 until elementCount) {
          resultSeq(i) shouldEqual dataSeries(i)
        }

        verifyCORSHeader()

      }

    }

    scenario("deviceId does not exists; Elasticsearch index does not exist either") {
      testGetHistoryAndDeviceDoesNotExist(None, None, indexExists = false)
    }

    scenario("deviceId does not exists; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(None, None, indexExists = true)
    }

  }

  feature(s"GET ${RouteConstants.urlDeviceHistory(":deviceId")}/:from") {

    scenario("deviceId exists") {

      // prepare
      val elementCount = 3
      val from = 1
      val dataSeries: List[DeviceMessage] = DeviceMessageTestUtil.storeSeries(elementCount).reverse
      val deviceId = dataSeries.head.deviceId

      // test
      Get(RouteConstants.urlDeviceHistoryFrom(deviceId, from)) ~> routes ~> check {

        // verify
        status shouldEqual OK

        responseEntity.contentType should be(`application/json`)
        val resultSeq = responseAs[Seq[DeviceMessage]]
        resultSeq.size shouldBe 2
        for (i <- 0 until 2) {
          resultSeq(i) shouldEqual dataSeries(i + from)
        }

        verifyCORSHeader()

      }

    }

    scenario("from is negative; Elasticsearch index does not exist") {

      // prepare
      val from = -1
      val deviceId = UUIDUtil.uuidStr

      // test
      Get(RouteConstants.urlDeviceHistoryFrom(deviceId, from)) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual MethodNotAllowed
        verifyCORSHeader(exist = false)

      }

    }

    scenario("from is negative; Elasticsearch index exists") {

      // prepare
      val messageSeries = DeviceMessageTestUtil.storeSeries(1)
      val from = -1
      val deviceId = messageSeries.head.deviceId

      // test
      Get(RouteConstants.urlDeviceHistoryFrom(deviceId, from)) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual MethodNotAllowed
        verifyCORSHeader(exist = false)

      }

    }

    scenario("deviceId does not exist; from < 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(-1), None, indexExists = false)
    }

    scenario("deviceId does not exist; from = 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(0), None, indexExists = false)
    }

    scenario("deviceId does not exist; from > 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(1), None, indexExists = false)
    }

    scenario("deviceId does not exist; from < 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(-1), None, indexExists = true)
    }

    scenario("deviceId does not exist; from = 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(0), None, indexExists = true)
    }

    scenario("deviceId does not exist; from > 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(1), None, indexExists = true)
    }

  }

  feature(s"GET ${RouteConstants.urlDeviceHistory(":deviceId")}/:from/:size") {

    scenario("deviceId exists; from = 0; size = elementCount") {
      val elementCount = 3
      val size = 3
      testGetHistoryWithFromSizeFindAll(elementCount, size)
    }

    scenario("deviceId exists; from = 0; size > elementCount") {
      val elementCount = 3
      val size = 4
      testGetHistoryWithFromSizeFindAll(elementCount, size)
    }

    // TODO test case: from = 0; size < elementCount

    scenario("deviceId exists; from < 0; size > 0") {

      // prepare
      val messageSeries = DeviceMessageTestUtil.storeSeries(1)
      val from = -1
      val size = 10
      val deviceId = messageSeries.head.deviceId

      // test
      Get(RouteConstants.urlDeviceHistoryFromSize(deviceId, from, size)) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual MethodNotAllowed
        verifyCORSHeader(exist = false)

      }

    }

    scenario("deviceId exists; from = 0; size < 0") {

      // prepare
      val messageSeries = DeviceMessageTestUtil.storeSeries(1)
      val from = 0
      val size = -1
      val deviceId = messageSeries.head.deviceId

      // test
      Get(RouteConstants.urlDeviceHistoryFromSize(deviceId, from, size)) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual MethodNotAllowed
        verifyCORSHeader(exist = false)

      }

    }

    scenario("deviceId exists; from < 0; size < 0") {

      // prepare
      val messageSeries = DeviceMessageTestUtil.storeSeries(1)
      val from = -1
      val size = -1
      val deviceId = messageSeries.head.deviceId

      // test
      Get(RouteConstants.urlDeviceHistoryFromSize(deviceId, from, size)) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual MethodNotAllowed
        verifyCORSHeader(exist = false)

      }

    }

    scenario("deviceId does not exist; from < 0; size < 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(-1), Some(-1), indexExists = false)
    }

    scenario("deviceId does not exist; from < 0; size = 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(-1), Some(0), indexExists = false)
    }

    scenario("deviceId does not exist; from < 0; size > 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(-1), Some(10), indexExists = false)
    }

    scenario("deviceId does not exist; from = 0; size < 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(0), Some(-1), indexExists = false)
    }

    scenario("deviceId does not exist; from = 0; size = 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(0), Some(0), indexExists = false)
    }

    scenario("deviceId does not exist; from = 0; size > 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(0), Some(10), indexExists = false)
    }

    scenario("deviceId does not exist; from > 0; size < 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(5), Some(-1), indexExists = false)
    }

    scenario("deviceId does not exist; from > 0; size = 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(5), Some(0), indexExists = false)
    }

    scenario("deviceId does not exist; from > 0; size > 0; Elasticsearch index does not exist") {
      testGetHistoryAndDeviceDoesNotExist(Some(5), Some(10), indexExists = false)
    }

    scenario("deviceId does not exist; from < 0; size < 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(-1), Some(-1), indexExists = true)
    }

    scenario("deviceId does not exist; from < 0; size = 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(-1), Some(0), indexExists = true)
    }

    scenario("deviceId does not exist; from < 0; size > 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(-1), Some(10), indexExists = true)
    }

    scenario("deviceId does not exist; from = 0; size < 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(0), Some(-1), indexExists = true)
    }

    scenario("deviceId does not exist; from = 0; size = 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(0), Some(0), indexExists = true)
    }

    scenario("deviceId does not exist; from = 0; size > 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(0), Some(10), indexExists = true)
    }

    scenario("deviceId does not exist; from > 0; size < 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(5), Some(-1), indexExists = true)
    }

    scenario("deviceId does not exist; from > 0; size = 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(5), Some(0), indexExists = true)
    }

    scenario("deviceId does not exist; from > 0; size > 0; Elasticsearch index exists") {
      testGetHistoryAndDeviceDoesNotExist(Some(5), Some(10), indexExists = true)
    }

  }

  feature(s"POST ${RouteConstants.urlDeviceHistory}") {

    ignore("insert message (messageId does not exist yet)") {
      // TODO write test
    }

    ignore("update message (messageId already exists)") {
      // TODO write test
    }

  }

  private def testGetHistoryWithFromSizeFindAll(elementCount: Int, size: Int) = {

    // prepare
    val from = 0
    val dataSeries: List[DeviceMessage] = DeviceMessageTestUtil.storeSeries(elementCount).reverse
    val deviceId = dataSeries.head.deviceId

    // test
    Get(RouteConstants.urlDeviceHistoryFromSize(deviceId, from, size)) ~> routes ~> check {

      // verify
      status shouldEqual OK

      responseEntity.contentType should be(`application/json`)
      val resultSeq = responseAs[Seq[DeviceMessage]]
      resultSeq.size shouldBe dataSeries.size
      for (i <- dataSeries.indices) {
        resultSeq(i) shouldEqual dataSeries(i)
      }

      verifyCORSHeader()

    }

  }

  private def testGetHistoryAndDeviceDoesNotExist(fromOpt: Option[Int],
                                                  sizeOpt: Option[Int],
                                                  indexExists: Boolean
                                                 ): Unit = {

    // prepare
    if (indexExists) {
      DeviceMessageTestUtil.storeSeries(1)
    }
    val deviceId = "1234asdf"

    val url = fromOpt match {

      case Some(from) =>
        sizeOpt match {
          case Some(size) => RouteConstants.urlDeviceHistoryFromSize(deviceId, from, size)
          case None => RouteConstants.urlDeviceHistoryFrom(deviceId, from)
        }

      case None => RouteConstants.urlDeviceHistory(deviceId)

    }

    // test
    Get(url) ~> Route.seal(routes) ~> check {

      // verify
      (fromOpt.isDefined && fromOpt.get < 0) || (sizeOpt.isDefined && sizeOpt.get < 0) match {

        case true => verifyMethodNotAllowed()

        case false =>

          indexExists match {
            case true => verifyBadRequestDeviceNotFound(deviceId, fromOpt, sizeOpt)
            case false => verifyInternalServerError()

          }

      }

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
