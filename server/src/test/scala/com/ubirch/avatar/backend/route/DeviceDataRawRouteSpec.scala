package com.ubirch.avatar.backend.route

import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.core.server.util.RouteConstants
import com.ubirch.avatar.model.DummyDeviceDataRaw
import com.ubirch.avatar.model.device.DeviceDataRaw
import com.ubirch.avatar.test.base.{ElasticsearchSpec, RouteSpec}

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
class DeviceDataRawRouteSpec extends RouteSpec
  with ElasticsearchSpec {

  private val routes = (new MainRoute).myRoute

  feature(s"POST ${RouteConstants.urlDeviceDataRaw}") {

    scenario("insert message (messageId does not exist yet)") {

      // prepare
      val deviceRaw = DummyDeviceDataRaw.data()

      // test
      Post(RouteConstants.urlDeviceDataRaw, deviceRaw) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual OK
        verifyCORSHeader()
        val storedDeviceRaw = responseAs[DeviceDataRaw]
        storedDeviceRaw shouldEqual deviceRaw.copy(messageId = storedDeviceRaw.messageId)

        Thread.sleep(1000)

        val deviceRawList = Await.result(DeviceDataRawManager.history(deviceRaw.deviceId), 1 seconds)
        deviceRawList.size should be(1)
        deviceRawList.head should be(storedDeviceRaw)

      }

    }

    scenario("ensure messageId is ignored: store DeviceDataRaw with existing messageId") {

      // prepare
      val deviceRaw1 = DummyDeviceDataRaw.data()
      val storedDeviceRaw1 = Await.result(DeviceDataRawManager.store(deviceRaw1), 1 second).get
      val deviceRaw2 = DummyDeviceDataRaw.data(deviceId = storedDeviceRaw1.deviceId, messageId = storedDeviceRaw1.messageId)

      // test
      Post(RouteConstants.urlDeviceDataRaw, deviceRaw2) ~> Route.seal(routes) ~> check {

        // verify
        status shouldEqual OK
        verifyCORSHeader()
        val storedDeviceRaw2 = responseAs[DeviceDataRaw]
        storedDeviceRaw2 shouldEqual deviceRaw2.copy(messageId = storedDeviceRaw2.messageId)

        Thread.sleep(1000)
        val deviceRawList = Await.result(DeviceDataRawManager.history(deviceRaw2.deviceId), 1 seconds)
        deviceRawList.size should be(2)

        deviceRawList.head should be(storedDeviceRaw2)
        deviceRawList(1) should be(storedDeviceRaw1)

      }

    }

  }

}
