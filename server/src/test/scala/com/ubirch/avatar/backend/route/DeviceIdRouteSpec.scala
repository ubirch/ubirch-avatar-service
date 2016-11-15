package com.ubirch.avatar.backend.route

import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.DummyDevices
import com.ubirch.avatar.model.device.Device
import com.ubirch.avatar.model.util.{ErrorFactory, ErrorResponse}
import com.ubirch.avatar.server.util.RouteConstants
import com.ubirch.avatar.test.base.RouteSpec

import org.scalatest.{BeforeAndAfterAll, Matchers}

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

/**
  * author: cvandrei
  * since: 2016-09-21
  */
class DeviceIdRouteSpec extends RouteSpec
  with Matchers
  with BeforeAndAfterAll {

  private val routes = (new MainRoute).myRoute

  feature(s"GET ${RouteConstants.urlDevice}/:deviceId") {

    scenario("without deviceId") {
      Get(RouteConstants.urlDeviceWithId("")) ~> routes ~> check {
        handled shouldEqual false
      }
    }

    scenario("deviceId exists") {

      val device = DummyDevices.device1
      val deviceId = device.deviceId

      Get(RouteConstants.urlDeviceWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Device] shouldEqual device
        verifyCORSHeader()
      }

    }

    scenario("deviceId does not exists") {

      val deviceId = DummyDevices.device1.deviceId + "-does-not-exist"

      Get(RouteConstants.urlDeviceWithId(deviceId)) ~> routes ~> check {

        val s = status
        val r = response
        s shouldEqual BadRequest

        val expectedError = ErrorFactory.create("QueryError", s"deviceId not found: deviceId=$deviceId")
        responseEntity.contentType should be(`application/json`)
        responseAs[ErrorResponse] shouldEqual expectedError

        verifyCORSHeader()

      }

    }

  }

  feature(s"PUT ${RouteConstants.urlDevice}/:deviceId") {

    scenario("without deviceId") {
      Put(RouteConstants.urlDeviceWithId("")) ~> routes ~> check {
        handled shouldEqual false
      }
    }

    scenario("deviceId exists") {

      val device = DummyDevices.device1
      val deviceId = device.deviceId

      Put(RouteConstants.urlDeviceWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Device] shouldEqual device
        verifyCORSHeader()
      }

    }

    scenario("deviceId does not exist") {

      val deviceId = DummyDevices.device1.deviceId + "-does-not-exist"

      Put(RouteConstants.urlDeviceWithId(deviceId)) ~> routes ~> check {

        status shouldEqual BadRequest

        val expectedError = ErrorFactory.create("UpdateError", s"failed to update device: deviceId=$deviceId")
        responseEntity.contentType should be(`application/json`)
        responseAs[ErrorResponse] shouldEqual expectedError

        verifyCORSHeader()

      }

    }

  }

  feature(s"DELETE ${RouteConstants.urlDevice}/:deviceId") {

    scenario("without deviceId") {
      Delete(RouteConstants.urlDeviceWithId("")) ~> routes ~> check {
        handled shouldEqual false
      }
    }

    scenario("deviceId exists") {

      val device = DummyDevices.device1
      val deviceId = device.deviceId

      Delete(RouteConstants.urlDeviceWithId(deviceId)) ~> routes ~> check {
        status shouldEqual OK
        responseEntity.contentType should be(`application/json`)
        responseAs[Device] shouldEqual device
        verifyCORSHeader()
      }

    }

    scenario("deviceId does not exist") {

      val deviceId = DummyDevices.device1.deviceId + "-does-not-exist"

      Delete(RouteConstants.urlDeviceWithId(deviceId)) ~> routes ~> check {

        status shouldEqual BadRequest

        val expectedError = ErrorFactory.create("DeleteError", s"failed to delete device: deviceId=$deviceId")
        responseEntity.contentType should be(`application/json`)
        responseAs[ErrorResponse] shouldEqual expectedError

        verifyCORSHeader()

      }

    }

  }

  override protected def beforeAll(): Unit = {
    DeviceManager.create(DummyDevices.device1)
  }
}
