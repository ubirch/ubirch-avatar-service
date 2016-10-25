package com.ubirch.avatar.core.device

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.{DeviceData, DummyDeviceData}
import com.ubirch.avatar.test.base.UnitSpec
import com.ubirch.services.storage.DeviceDataStorage
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.uuid.UUIDUtil

import org.json4s.JsonAST.JValue
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.write

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-10-25
  */
class DeviceDataManagerSpec extends UnitSpec
  with MyJsonProtocol {

  feature("history()") {

    scenario("deviceId empty") {
      an[IllegalArgumentException] should be thrownBy Await.result(DeviceDataManager.history(""), 1 seconds)
    }

    scenario("deviceId does not exist") {
      val deviceId = UUIDUtil.uuidStr
      Await.result(DeviceDataManager.history(deviceId), 1 seconds) should be(Seq.empty)
    }

    scenario("3 records exist: from = -1; size > 3") {

      // prepare
      val dataSeries: List[DeviceData] = DummyDeviceData.dataSeries(elementCount = 3)
      val deviceId: String = dataSeries.head.deviceId
      store(dataSeries)

      // test
      val result: Seq[DeviceData] = Await.result(DeviceDataManager.history(deviceId), 2 seconds)

      // verify
      result should be('nonEmpty)
      // TODO finish and fix test

    }

    ignore("3 records exist: from = 0; size > 3") {
      // TODO write test
    }

    ignore("3 records exist: from = 0; size = 0") {
      // TODO write test
    }

    ignore("3 records exist: from = 1; size > 3") {
      // TODO write test
    }

    ignore("3 records exist: from = 3; size > 3") {
      // TODO write test
    }

  }

  private def store(dataSeries: List[DeviceData]) = {

    dataSeries foreach { deviceData =>
      val doc: JValue = parse(write[DeviceData](deviceData))
      DeviceDataStorage.storeDoc(docIndex = Config.deviceDataDbIndex, docType = deviceData.deviceId, doc = doc) map println
    }

    Thread.sleep(3000)

  }

}
