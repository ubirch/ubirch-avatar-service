package com.ubirch.avatar.backend.route

import com.ubirch.avatar.core.server.util.RouteConstants
import com.ubirch.avatar.model.device.DeviceType
import com.ubirch.avatar.model.server.JsonErrorResponse
import com.ubirch.avatar.test.base.{ElasticsearchSpec, RouteSpec}
import com.ubirch.avatar.util.model.DeviceTypeUtil

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

  feature(s"GET ${RouteConstants.urlDeviceType}") {

    scenario("index does not exist --> empty response") {
      deleteIndexes()
      runTypeGetProducesEmptyResponse()
    }

    scenario("index exists; no records exist --> empty response") {
      runTypeGetProducesEmptyResponse()
    }

    ignore("some records exist") {
      // TODO
    }

  }

  feature(s"POST ${RouteConstants.urlDeviceType}") {

    scenario("index does not exist --> create is successful") {
      deleteIndexes()
      runTypePostCreatesRecord()
    }

    scenario("index exists; no record with given key exists --> create is successful") {
      runTypePostCreatesRecord()
    }

    ignore("record with given key exists --> create fails") {
      // TODO
    }

  }

  feature(s"PUT ${RouteConstants.urlDeviceType}") {

    scenario("index does not exist --> update fails") {
      deleteIndexes()
      runTypePutFails()
    }

    scenario("index exists; no record with given key exists --> update fails") {
      runTypePutFails()
    }

    ignore("record with given key exists --> update is successful") {
      // TODO
    }

  }

  feature(s"GET ${RouteConstants.urlDeviceTypeInit}") {

    scenario("index does not exist --> default deviceTypes are created") {
      deleteIndexes()
      runTypeInitCreatesRecords()
    }

    scenario("index exists; no records exist --> default deviceTypes are created") {
      runTypeInitCreatesRecords()
    }

    ignore("records exist --> no deviceTypes are created") {
      // TODO
    }

  }

  private def runTypeGetProducesEmptyResponse() = {

    // test
    Get(RouteConstants.urlDeviceType) ~> Route.seal(routes) ~> check {

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
    Post(RouteConstants.urlDeviceType, deviceType) ~> Route.seal(routes) ~> check {

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
    Put(RouteConstants.urlDeviceType, deviceType) ~> Route.seal(routes) ~> check {

      // verify
      status shouldEqual BadRequest

      verifyCORSHeader()

      responseEntity.contentType should be(`application/json`)
      responseAs[JsonErrorResponse] should be(JsonErrorResponse(errorType = "UpdateError", errorMessage = s"no deviceType with key=${deviceType.key} exists or otherwise something else on the server went wrong"))

    }

  }

  private def runTypeInitCreatesRecords() = {

    // test
    Get(RouteConstants.urlDeviceTypeInit) ~> Route.seal(routes) ~> check {

      // verify
      status shouldEqual OK

      verifyCORSHeader()

      responseEntity.contentType should be(`application/json`)
      responseAs[Seq[DeviceType]] should be(DeviceTypeUtil.defaultDeviceTypes.toSeq)

    }

  }

}
