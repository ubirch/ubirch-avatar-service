package com.ubirch.avatar.backend.route

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import com.ubirch.avatar.model.{DummyDeviceDataRaw, DummyDevices}
import com.ubirch.avatar.test.base.{ElasticsearchSpec, RouteSpec}
import com.ubirch.avatar.util.server.RouteConstants
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-09-30
  */
class DeviceDataRawRouteSpec extends RouteSpec
  with ElasticsearchSpec {


  ignore(s"POST ${RouteConstants.pathDeviceDataRaw}") {
    val routes = (new MainRoute).myRoute
    scenario("insert message (messageId does not exist yet)") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val deviceRaw = DummyDeviceDataRaw.data(device = device)

      // test
      Post(RouteConstants.pathDeviceDataRaw, deviceRaw) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual OK
        verifyCORSHeader()
        val storedRaw = responseAs[DeviceDataRaw]
        storedRaw shouldEqual deviceRaw.copy(id = storedRaw.id)

        Thread.sleep(1000)
        val rawList = Await.result(DeviceDataRawManager.history(device), 1 seconds)
        rawList.size should be(1)
        rawList.head should be(storedRaw)

      }

    }

    scenario("ensure messageId is ignored: store DeviceDataRaw with existing messageId") {

      // prepare
      val device = DummyDevices.minimalDevice()
      val raw1 = DummyDeviceDataRaw.data(device = device)

      val storedRaw1 = DeviceDataRawManager.store(raw1).get
      val raw2 = DummyDeviceDataRaw.data(messageId = storedRaw1.id, device = device)

      // test
      Post(RouteConstants.pathDeviceDataRaw, raw2) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual OK
        verifyCORSHeader()
        val storedRaw2 = responseAs[DeviceDataRaw]
        storedRaw2 shouldEqual raw2.copy(id = storedRaw2.id)

        storedRaw1 shouldBe true
        Thread.sleep(1000)
        val rawList = Await.result(DeviceDataRawManager.history(device), 1 seconds)
        rawList.size should be(2)

        rawList.head should be(storedRaw2)
        rawList(1) should be(raw1)

      }

    }

  }

}
