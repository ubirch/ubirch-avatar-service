package com.ubirch.avatar.core.device

import java.util.UUID

import com.ubirch.avatar.core.test.model.DummyDeviceHistory
import com.ubirch.avatar.core.test.util.DeviceHistoryTestUtil
import com.ubirch.avatar.model.rest.device.DeviceHistory
import com.ubirch.avatar.test.base.ElasticsearchSpec
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-10-25
  */
class DeviceHistoryManagerSpec
  extends ElasticsearchSpec
    with MyJsonProtocol {

  feature("store()") {

    scenario("messageId does not exist before") {

      // prepare
      val deviceHistory = DummyDeviceHistory.data()

      // test
      val response = DeviceHistoryManager.store(deviceHistory).get
      Thread.sleep(1500)

      // verify
      val deviceHistoryList = Await.result(DeviceHistoryManager.history(response.deviceId), 1 seconds)
      deviceHistoryList.size should be(1)

      deviceHistoryList.head should be(response)

    }

    scenario("make sure that messageId is ignored: try to store object with same messageId twice") {

      // prepare
      val deviceHistory1 = DummyDeviceHistory.data()
      val storedDeviceHistory1 = DeviceHistoryManager.store(deviceHistory1).get

      val deviceHistory2 = DummyDeviceHistory.data(
        deviceId = storedDeviceHistory1.deviceId,
        messageId = storedDeviceHistory1.messageId
      )

      // test
      val response = DeviceHistoryManager.store(deviceHistory2).get
      Thread.sleep(1500)

      // verify
      val deviceHistoryList = Await.result(DeviceHistoryManager.history(deviceHistory2.deviceId), 1 seconds)
      deviceHistoryList.size should be(2)

      deviceHistoryList.head should be(response)
      deviceHistoryList(1) should be(storedDeviceHistory1)

    }

  }

  feature("history()") {

    scenario("deviceId empty") {
      an[IllegalArgumentException] should be thrownBy Await.result(DeviceHistoryManager.history(""), 1 seconds)
    }

    scenario("deviceId does not exist; index does not exist") {
      val deviceId = UUIDUtil.uuidStr
      Await.result(DeviceHistoryManager.history(deviceId), 1 seconds) should be('isEmpty)
    }

    scenario("deviceId does not exist; index exists") {

      // prepare
      DeviceHistoryTestUtil.storeSeries(1)
      val deviceId = UUIDUtil.uuidStr

      // test
      val result: Seq[DeviceHistory] = Await.result(DeviceHistoryManager.history(deviceId), 2 seconds)

      // verify
      result should be('isEmpty)

    }

    scenario("3 records exist: from = -1; size > 3") {
      testWithInvalidFromOrSize(elementCount = 3, from = -1, size = 4)
    }

    scenario("3 records exist: from = 0; size = -1") {
      testWithInvalidFromOrSize(elementCount = 3, from = 0, size = -1)
    }

    scenario("3 records exist: from = 0; size = 0") {

      // prepare
      val elementCount = 3
      val from = 0
      val size = 0
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(elementCount)
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceHistory] = Await.result(DeviceHistoryManager.history(deviceId, from, size), 2 seconds)

      // verify
      result should be('isEmpty)

    }

    scenario("3 records exist: from = 0; size > 3") {

      // prepare
      val elementCount = 3
      val from = 0
      val size = elementCount + 1
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(elementCount).reverse
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceHistory] = Await.result(DeviceHistoryManager.history(deviceId, from, size), 2 seconds)

      // verify
      result.size should be(3)
      for (i <- dataSeries.indices) {
        result(i) shouldEqual dataSeries(i)
      }

    }

    scenario("3 records exist: from = 1; size > 3") {

      // prepare
      val elementCount = 3
      val from = 1
      val size = elementCount + 1
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(elementCount).reverse
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceHistory] = Await.result(DeviceHistoryManager.history(deviceId, from, size), 2 seconds)

      // verify
      result.size should be(2)
      result.head shouldEqual dataSeries(1)
      result(1) shouldEqual dataSeries(2)

    }

    scenario("3 records exist: from = 3; size > 3") {

      // prepare
      val elementCount = 3
      val from = elementCount
      val size = elementCount + 1
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(elementCount)
      val deviceId: String = dataSeries.head.deviceId

      // test
      val result: Seq[DeviceHistory] = Await.result(DeviceHistoryManager.history(deviceId, from, size), 2 seconds)

      // verify
      result should be('isEmpty)

    }

  }

  feature("byDate()") {

    scenario("deviceId does not exist; index does not exist") {
      deleteIndices()
      val result = Await.result(DeviceHistoryManager.byDate(UUIDUtil.uuid, DateTime.now, DateTime.now), 1 seconds)
      result should be('isEmpty)
    }

    scenario("deviceId does not exist; index exists") {
      val result = Await.result(DeviceHistoryManager.byDate(UUIDUtil.uuid, DateTime.now, DateTime.now), 1 seconds)
      result should be('isEmpty)
    }

    scenario("all 3 records in interval") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(3)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val from = dataSeries.head.timestamp.minusSeconds(10)
      val to = dataSeries.last.timestamp.plusSeconds(10)

      // test
      val result = Await.result(DeviceHistoryManager.byDate(deviceId, from, to), 1 seconds)

      // verify
      result should be(dataSeries.reverse)

    }

    scenario("5000 records in interval (ensure that Elasticsearch page size is not the default)") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(5000)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val from = dataSeries.head.timestamp.minusSeconds(10)
      val to = dataSeries.last.timestamp.plusSeconds(10)

      // test
      val result = Await.result(DeviceHistoryManager.byDate(deviceId, from, to), 3 seconds)

      // verify
      result.size should be(dataSeries.size)

    }

    scenario("all records in interval; first at lower boundary") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(3)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val from = dataSeries.head.timestamp
      val to = dataSeries.last.timestamp.plusSeconds(10)

      // test
      val result = Await.result(DeviceHistoryManager.byDate(deviceId, from, to), 1 seconds)

      // verify
      result should be(dataSeries.reverse)

    }

    scenario("all records in interval; except for: first before lower boundary") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(3)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val from = dataSeries.head.timestamp.plusMillis(1)
      val to = dataSeries.last.timestamp.plusSeconds(10)

      // test
      val result = Await.result(DeviceHistoryManager.byDate(deviceId, from, to), 1 seconds)

      // verify
      result should be(dataSeries.tail.reverse)

    }

    scenario("all records in interval; last at upper boundary") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(3)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val from = dataSeries.head.timestamp.minusSeconds(10)
      val to = dataSeries.last.timestamp

      // test
      val result = Await.result(DeviceHistoryManager.byDate(deviceId, from, to), 1 seconds)

      // verify
      result should be(dataSeries.reverse)

    }

    scenario("all records in interval; except for: last after upper boundary") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(3)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val from = dataSeries.head.timestamp.minusSeconds(10)
      val to = dataSeries.last.timestamp.minusMillis(1)

      // test
      val result = Await.result(DeviceHistoryManager.byDate(deviceId, from, to), 1 seconds)

      // verify
      val expected = Seq(dataSeries.head, dataSeries(1)).reverse
      result should be(expected)

    }

  }

  feature("before()") {

    scenario("deviceId does not exist; index does not exist") {
      deleteIndices()
      val result = Await.result(DeviceHistoryManager.before(UUIDUtil.uuid, DateTime.now), 1 seconds)
      result should be('isEmpty)
    }

    scenario("deviceId does not exist; index exists") {
      val result = Await.result(DeviceHistoryManager.before(UUIDUtil.uuid, DateTime.now), 1 seconds)
      result should be('isEmpty)
    }

    scenario("all 3 records in interval") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(3)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val before = dataSeries.last.timestamp.plusSeconds(10)

      // test
      val result = Await.result(DeviceHistoryManager.before(deviceId, before), 1 seconds)

      // verify
      result should be(dataSeries.reverse)

    }

    scenario("5000 records in interval (ensure that Elasticsearch page size is not the default)") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(5000)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val before = dataSeries.last.timestamp.plusSeconds(10)

      // test
      val result = Await.result(DeviceHistoryManager.before(deviceId, before), 3 seconds)

      // verify
      result.size should be(dataSeries.size)

    }

    scenario("all records in interval; except for: last at upper boundary") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(3)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val before = dataSeries.last.timestamp

      // test
      val result = Await.result(DeviceHistoryManager.before(deviceId, before), 1 seconds)

      // verify
      val expected = Seq(dataSeries.head, dataSeries(1)).reverse
      result should be(expected)

    }

    scenario("all records in interval; except for: last after upper boundary") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(3)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val before = dataSeries.last.timestamp.minusMillis(1)

      // test
      val result = Await.result(DeviceHistoryManager.before(deviceId, before), 1 seconds)

      // verify
      val expected = Seq(dataSeries.head, dataSeries(1)).reverse
      result should be(expected)

    }

  }

  feature("after()") {

    scenario("deviceId does not exist; index does not exist") {
      deleteIndices()
      val result = Await.result(DeviceHistoryManager.after(UUIDUtil.uuid, DateTime.now), 1 seconds)
      result should be('isEmpty)
    }

    scenario("deviceId does not exist; index exists") {
      val result = Await.result(DeviceHistoryManager.after(UUIDUtil.uuid, DateTime.now), 1 seconds)
      result should be('isEmpty)
    }

    scenario("all 3 records in interval") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(3)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val after = dataSeries.head.timestamp.minusSeconds(10)

      // test
      val result = Await.result(DeviceHistoryManager.after(deviceId, after), 1 seconds)

      // verify
      result should be(dataSeries.reverse)

    }

    scenario("5000 records in interval (ensure that Elasticsearch page size is not the default)") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(5000)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val after = dataSeries.head.timestamp.minusSeconds(10)

      // test
      val result = Await.result(DeviceHistoryManager.after(deviceId, after), 3 seconds)

      // verify
      result.size should be(dataSeries.size)

    }

    scenario("all records in interval; first at lower boundary") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(3)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val after = dataSeries.head.timestamp.minusSeconds(10)

      // test
      val result = Await.result(DeviceHistoryManager.after(deviceId, after), 1 seconds)

      // verify
      result should be(dataSeries.reverse)

    }

    scenario("all records in interval; except for: first before lower boundary") {

      // prepare
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(3)
      val deviceId: UUID = UUIDUtil.fromString(dataSeries.head.deviceId)
      val after = dataSeries.head.timestamp.plusMillis(1)

      // test
      val result = Await.result(DeviceHistoryManager.after(deviceId, after), 1 seconds)

      // verify
      result should be(dataSeries.tail.reverse)

    }

  }

  feature("byDay()") {

    scenario("deviceId does not exist; index does not exist") {
      deleteIndices()
      val result = Await.result(DeviceHistoryManager.byDay(UUIDUtil.uuid, DateTime.now), 1 seconds)
      result should be('isEmpty)
    }

    scenario("deviceId does not exist; index exists") {
      val result = Await.result(DeviceHistoryManager.byDay(UUIDUtil.uuid, DateTime.now), 1 seconds)
      result should be('isEmpty)
    }

    scenario("all 3 records in interval (on the first millisecond of the day)") {

      // prepare
      val deviceId: UUID = UUIDUtil.uuid
      val dayBegins = firstMillisecondOfToday()

      val t1 = dayBegins.plusHours(2)
      val t2 = dayBegins.plusHours(4).plusMinutes(10).plusSeconds(5).plusMillis(23)
      val t3 = dayBegins.plusHours(21).plusMinutes(42).plusSeconds(27).plusMillis(46)
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeTimeBasedSeries(deviceId, Seq(t1, t2, t3))

      // test
      val result = Await.result(DeviceHistoryManager.byDay(deviceId, dayBegins), 1 seconds)

      // verify
      result should be(dataSeries.reverse)

    }

    scenario("all 3 records in interval (on the last millisecond of the day)") {

      // prepare
      val deviceId: UUID = UUIDUtil.uuid
      val dayBegins = firstMillisecondOfToday()
      val dayEnds = dayBegins.plusDays(1).minusMillis(1)

      val t1 = dayBegins.plusHours(2)
      val t2 = dayBegins.plusHours(4).plusMinutes(10).plusSeconds(5).plusMillis(23)
      val t3 = dayBegins.plusHours(21).plusMinutes(42).plusSeconds(27).plusMillis(46)
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeTimeBasedSeries(deviceId, Seq(t1, t2, t3))

      // test
      val result = Await.result(DeviceHistoryManager.byDay(deviceId, dayEnds), 1 seconds)

      // verify
      result should be(dataSeries.reverse)

    }

    scenario("5000 records in interval (ensure that Elasticsearch page size is not the default)") {

      // prepare
      val deviceId: UUID = UUIDUtil.uuid
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(
        elementCount = 5000,
        deviceId = deviceId.toString
      )

      val day = dataSeries.head.timestamp

      // test
      val result = Await.result(DeviceHistoryManager.byDay(deviceId, day), 3 seconds)

      // verify
      result.size should be(dataSeries.size)

    }

    scenario("all records in interval; first at lower boundary") {

      // prepare
      val deviceId: UUID = UUIDUtil.uuid
      val midnight = firstMillisecondOfToday()

      val t1 = midnight
      val t2 = midnight.plusHours(4).plusMinutes(10).plusSeconds(5).plusMillis(23)
      val t3 = midnight.plusHours(21).plusMinutes(42).plusSeconds(27).plusMillis(46)
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeTimeBasedSeries(deviceId, Seq(t1, t2, t3))

      val day = midnight.plusHours(7)

      // test
      val result = Await.result(DeviceHistoryManager.byDay(deviceId, day), 1 seconds)

      // verify
      result should be(dataSeries.reverse)

    }

    scenario("all records in interval; except for: first before lower boundary") {

      // prepare
      val deviceId: UUID = UUIDUtil.uuid
      val midnight = firstMillisecondOfToday()

      val t1 = midnight.minusMillis(1)
      val t2 = midnight.plusHours(4).plusMinutes(10).plusSeconds(5).plusMillis(23)
      val t3 = midnight.plusHours(21).plusMinutes(42).plusSeconds(27).plusMillis(46)
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeTimeBasedSeries(deviceId, Seq(t1, t2, t3))

      val day = midnight.plusHours(7)

      // test
      val result = Await.result(DeviceHistoryManager.byDay(deviceId, day), 1 seconds)

      // verify
      result should be(dataSeries.tail.reverse)

    }

    scenario("all records in interval; last at upper boundary") {

      // prepare
      val deviceId: UUID = UUIDUtil.uuid
      val midnight = firstMillisecondOfToday()

      val t1 = midnight.plusHours(2)
      val t2 = midnight.plusHours(4).plusMinutes(10).plusSeconds(5).plusMillis(23)
      val t3 = midnight.plusDays(1).minusMillis(1)
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeTimeBasedSeries(deviceId, Seq(t1, t2, t3))

      val day = midnight.plusHours(7)

      // test
      val result = Await.result(DeviceHistoryManager.byDay(deviceId, day), 1 seconds)

      // verify
      result should be(dataSeries.reverse)

    }

    scenario("all records in interval; except for: last after upper boundary") {

      // prepare
      val deviceId: UUID = UUIDUtil.uuid
      val midnight = firstMillisecondOfToday()

      val t1 = midnight.plusHours(2)
      val t2 = midnight.plusHours(4).plusMinutes(10).plusSeconds(5).plusMillis(23)
      val t3 = midnight.plusDays(1)
      val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeTimeBasedSeries(deviceId, Seq(t1, t2, t3))

      val day = midnight.plusHours(7)

      // test
      val result = Await.result(DeviceHistoryManager.byDay(deviceId, day), 1 seconds)

      // verify
      val expected = Seq(dataSeries.head, dataSeries(1)).reverse
      result should be(expected)

    }

  }

  private def testWithInvalidFromOrSize(elementCount: Int, from: Int, size: Int) = {

    // prepare
    val dataSeries: Seq[DeviceHistory] = DeviceHistoryTestUtil.storeSeries(elementCount)
    val deviceId: String = dataSeries.head.deviceId

    // test && verify
    an[IllegalArgumentException] should be thrownBy Await.result(DeviceHistoryManager.history(deviceId, from, size), 1 seconds)

  }

  private def firstMillisecondOfToday(): DateTime = {
    DateTime.now(DateTimeZone.UTC)
      .withHourOfDay(0)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)
  }

}
