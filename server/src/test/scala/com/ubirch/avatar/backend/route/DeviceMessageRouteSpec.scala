package com.ubirch.avatar.backend.route

import com.ubirch.avatar.core.server.util.RouteConstants
import com.ubirch.avatar.core.test.util.DeviceMessageTestUtil
import com.ubirch.avatar.model.device.DeviceMessage
import com.ubirch.avatar.model.util.{ErrorFactory, ErrorResponse}
import com.ubirch.avatar.test.base.{ElasticsearchSpec, RouteSpec}

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
      testGetHistoryDeviceExists(3, None, None)
    }

    scenario("deviceId does not exists; Elasticsearch index does not exist either") {
      testGetHistoryDeviceExistsNot(None, None, indexExists = false)
    }

    scenario("deviceId does not exists; Elasticsearch index exists") {
      testGetHistoryDeviceExistsNot(None, None, indexExists = true)
    }

  }

  feature(s"GET ${RouteConstants.urlDeviceHistory(":deviceId")}/:from") {

    ignore("deviceId exists; from < 0") {
      // TODO write test
    }

    scenario("deviceId exists; from = 0") {
      testGetHistoryDeviceExists(3, Some(0), None)
    }

    scenario("deviceId exists; from > 0") {
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

    ignore("deviceId exists; from = 0; size < elementCount") {
      // TODO test case: from = 0; size < elementCount
    }

    scenario("deviceId exists; from = 0; size = elementCount") {
      testGetHistoryDeviceExists(3, Some(0), Some(3))
    }

    scenario("deviceId exists; from = 0; size > elementCount") {
      testGetHistoryDeviceExists(3, Some(0), Some(4))
    }

    scenario("deviceId exists; from = 0; size < 0") {
      testGetHistoryDeviceExists(1, Some(0), Some(10))
    }

    scenario("deviceId exists; from < 0; size < 0") {
      testGetHistoryDeviceExists(1, Some(-1), Some(-1))
    }

    scenario("deviceId exists; from < 0; size > 0") {
      testGetHistoryDeviceExists(1, Some(-1), Some(10))
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

    ignore("insert message (messageId does not exist yet)") {
      // TODO write test
    }

    ignore("update message (messageId already exists)") {
      // TODO write test
    }

  }

  private def testGetHistoryDeviceExists(elementCount: Int, from: Option[Int], size: Option[Int]) = {

    // prepare
    val dataSeries: List[DeviceMessage] = DeviceMessageTestUtil.storeSeries(elementCount).reverse
    val deviceId = dataSeries.head.deviceId
    val url = urlForTest(deviceId, from, size)

    // test
    Get(url) ~> Route.seal(routes) ~> check {

      // verify
      (from.isDefined && from.get < 0) || (size.isDefined && size.get < 0) match {

        case true => verifyMethodNotAllowed()

        case false =>

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

  }

  private def testGetHistoryDeviceExistsNot(from: Option[Int],
                                                  size: Option[Int],
                                                  indexExists: Boolean
                                                 ): Unit = {

    // prepare
    if (indexExists) {
      DeviceMessageTestUtil.storeSeries(1)
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
          case Some(size) => RouteConstants.urlDeviceHistoryFromSize(deviceId, from, size)
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
