package com.ubirch.avatar.history

import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

/**
  * author: cvandrei
  * since: 2016-10-28
  */
class HistoryIndexUtilSpec extends AnyFeatureSpec
  with Matchers {

  Feature("calculateEndIndex()") {

    Scenario("10 elements; from = 0, size = 10") {
      HistoryIndexUtil.calculateEndIndex(10, 0, 10) shouldEqual Some(9)
    }

    Scenario("10 elements; from = 2, size = 10") {
      HistoryIndexUtil.calculateEndIndex(10, 2, 10) shouldEqual Some(9)
    }

    Scenario("10 elements; from = 11, size = 10") {
      HistoryIndexUtil.calculateEndIndex(10, 11, 10) shouldEqual None
    }

    Scenario("10 elements; from = 11, size = 11") {
      HistoryIndexUtil.calculateEndIndex(10, 11, 11) shouldEqual None
    }

    Scenario("10 elements; from = 0, size = 9") {
      HistoryIndexUtil.calculateEndIndex(10, 0, 9) shouldEqual Some(8)
    }

    Scenario("10 elements; from = 0, size = 11") {
      HistoryIndexUtil.calculateEndIndex(10, 0, 11) shouldEqual Some(9)
    }

    Scenario("11 elements; from = 0, size = 10") {
      HistoryIndexUtil.calculateEndIndex(11, 0, 10) shouldEqual Some(9)
    }

    Scenario("11 elements; from = 1, size = 10") {
      HistoryIndexUtil.calculateEndIndex(11, 1, 10) shouldEqual Some(10)
    }

  }

  Feature("test tooling: calculateExpectedSize()") {

    Scenario("beginIndex = 0; endIndex = 9") {
      HistoryIndexUtil.calculateExpectedSize(0, 9) should be(10)
    }

    Scenario("beginIndex = 1; endIndex = 9") {
      HistoryIndexUtil.calculateExpectedSize(1, 9) should be(9)
    }

  }

}
