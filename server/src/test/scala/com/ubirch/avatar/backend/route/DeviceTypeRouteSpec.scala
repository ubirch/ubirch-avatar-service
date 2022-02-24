package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.model.rest.device.DeviceType
import com.ubirch.avatar.test.base.{ElasticsearchSpec, RouteSpec}
import com.ubirch.avatar.test.tools.DeviceTypeTestUtil
import com.ubirch.avatar.util.model.DeviceTypeUtil
import com.ubirch.avatar.util.server.RouteConstants
import com.ubirch.util.model.JsonErrorResponse
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-11-11
  */
class DeviceTypeRouteSpec extends RouteSpec
  with ElasticsearchSpec {


  //deviceTypeIndex index is not being used anymore
  ignore(s"GET ${RouteConstants.pathDeviceType}") {
    val routes = (new MainRoute).myRoute
    scenario("index does not exist --> empty response") {
      deleteIndices()
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

  //deviceTypeIndex index is not being used anymore
  ignore(s"POST ${RouteConstants.pathDeviceType}") {
    val routes = (new MainRoute).myRoute
    scenario("index does not exist --> create is successful") {
      deleteIndices()
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

  //deviceTypeIndex index is not being used anymore
  ignore(s"PUT ${RouteConstants.pathDeviceType}") {
    val routes = (new MainRoute).myRoute
    scenario("index does not exist --> update fails") {
      deleteIndices()
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

  //deviceTypeIndex index is not being used anymore
  ignore(s"GET ${RouteConstants.pathDeviceTypeInit}") {
    val routes = (new MainRoute).myRoute
    scenario("index does not exist --> default deviceTypes are created") {
      deleteIndices()
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
    val routes = (new MainRoute).myRoute
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
    val routes = (new MainRoute).myRoute
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
    val routes = (new MainRoute).myRoute
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
    val routes = (new MainRoute).myRoute
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
