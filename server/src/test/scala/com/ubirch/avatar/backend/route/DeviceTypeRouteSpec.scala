package com.ubirch.avatar.backend.route

import com.ubirch.avatar.core.test.util.DeviceTypeTestUtil
import com.ubirch.avatar.model.device.DeviceType
import com.ubirch.avatar.model.server.JsonErrorResponse
import com.ubirch.avatar.test.base.{ElasticsearchSpec, RouteSpec}
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.avatar.util.server.RouteConstants

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-11-11
  */
class DeviceTypeRouteSpec extends RouteSpec
  with ElasticsearchSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.pathDeviceType}") {

    scenario("index does not exist --> empty response") {
      deleteIndexes()
      runTypeGetProducesEmptyResponse()
    }

    scenario("index exists; no records exist --> empty response") {
      runTypeGetProducesEmptyResponse()
    }

    scenario("some records exist") {

      // prepare
      val deviceTypes = DeviceTypeTestUtil.storeSeries()

      // test
      Get(RouteConstants.pathDeviceType) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual OK

        verifyCORSHeader()

        responseEntity.contentType should be(`application/json`)
        responseAs[Set[DeviceType]] should be(deviceTypes)

      }

    }

  }

  feature(s"POST ${RouteConstants.pathDeviceType}") {

    scenario("index does not exist --> create is successful") {
      deleteIndexes()
      runTypePostCreatesRecord()
    }

    scenario("index exists; no record with given key exists --> create is successful") {
      runTypePostCreatesRecord()
    }

    scenario("record with given key exists --> create fails") {

      // prepare
      val deviceTypes = DeviceTypeTestUtil.storeSeries()
      val deviceType = deviceTypes.head

      // test
      Post(RouteConstants.pathDeviceType, deviceType) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual BadRequest

        verifyCORSHeader()

        responseEntity.contentType should be(`application/json`)
        responseAs[JsonErrorResponse] should be(JsonErrorResponse(errorType = "CreateError", errorMessage = s"another deviceType with key=${deviceType.key} already exists or otherwise something else on the server went wrong"))

      }

    }

  }

  feature(s"PUT ${RouteConstants.pathDeviceType}") {

    scenario("index does not exist --> update fails") {
      deleteIndexes()
      runTypePutFails()
    }

    scenario("index exists; no record with given key exists --> update fails") {
      runTypePutFails()
    }

    scenario("record with given key exists --> update is successful") {

      // prepare
      val deviceType1 = DeviceTypeTestUtil.storeSeries().head
      val deviceType = deviceType1.copy(icon = s"${deviceType1.icon}1")

      // test
      Put(RouteConstants.pathDeviceType, deviceType) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual OK

        verifyCORSHeader()

        responseEntity.contentType should be(`application/json`)
        responseAs[DeviceType] should be(deviceType)

      }

    }

  }

  feature(s"GET ${RouteConstants.pathDeviceTypeInit}") {

    scenario("index does not exist --> default deviceTypes are created") {
      deleteIndexes()
      runTypeInitCreatesRecords()
    }

    scenario("index exists; no records exist --> default deviceTypes are created") {
      runTypeInitCreatesRecords()
    }

    scenario("records exist --> no deviceTypes are created") {

      // prepare
      val deviceTypes = DeviceTypeTestUtil.storeSeries()

      // test
      Get(RouteConstants.pathDeviceTypeInit) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual OK

        verifyCORSHeader()

        responseEntity.contentType should be(`application/json`)
        responseAs[Set[DeviceType]] should be(deviceTypes)

      }

    }

  }

  private def runTypeGetProducesEmptyResponse() = {

    // test
    Get(RouteConstants.pathDeviceType) ~> Route.seal(routes) ~> check {

      // verify
      status shouldEqual OK

      verifyCORSHeader()

      responseEntity.contentType should be(`application/json`)
      responseAs[Seq[DeviceType]] should be('isEmpty)

    }

  }

  private def runTypePostCreatesRecord() = {

    // (continue) prepare
    val deviceType = DeviceTypeUtil.defaultDeviceType()

    // test
    Post(RouteConstants.pathDeviceType, deviceType) ~> Route.seal(routes) ~> check {

      // verify
      status shouldEqual OK

      verifyCORSHeader()

      responseEntity.contentType should be(`application/json`)
      responseAs[DeviceType] should be(deviceType)

    }

  }

  private def runTypePutFails() = {

    // (continue) prepare
    val deviceType = DeviceTypeUtil.defaultDeviceType()

    // test
    Put(RouteConstants.pathDeviceType, deviceType) ~> Route.seal(routes) ~> check {

      // verify
      status shouldEqual BadRequest

      verifyCORSHeader()

      responseEntity.contentType should be(`application/json`)
      responseAs[JsonErrorResponse] should be(JsonErrorResponse(errorType = "UpdateError", errorMessage = s"no deviceType with key=${deviceType.key} exists or otherwise something else on the server went wrong"))

    }

  }

  private def runTypeInitCreatesRecords() = {

    // test
    Get(RouteConstants.pathDeviceTypeInit) ~> Route.seal(routes) ~> check {

      // verify
      status shouldEqual OK

      verifyCORSHeader()

      responseEntity.contentType should be(`application/json`)
      responseAs[Seq[DeviceType]] should be(DeviceTypeUtil.defaultDeviceTypes.toSeq)

    }

  }

}
