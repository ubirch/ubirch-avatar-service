package com.ubirch.avatar.history

import org.scalatest.{FeatureSpec, Matchers}

/**
  * author: cvandrei
  * since: 2016-10-28
  */
class HistoryIndexUtilSpec extends FeatureSpec
  with Matchers {

  feature("calculateEndIndex()") {

    scenario("10 elements; from = 0, size = 10") {
      HistoryIndexUtil.calculateEndIndex(10, 0, 10) shouldEqual Some(9)
    }

    scenario("10 elements; from = 2, size = 10") {
      HistoryIndexUtil.calculateEndIndex(10, 2, 10) shouldEqual Some(9)
    }

    scenario("10 elements; from = 11, size = 10") {
      HistoryIndexUtil.calculateEndIndex(10, 11, 10) shouldEqual None
    }

    scenario("10 elements; from = 11, size = 11") {
      HistoryIndexUtil.calculateEndIndex(10, 11, 11) shouldEqual None
    }

    scenario("10 elements; from = 0, size = 9") {
      HistoryIndexUtil.calculateEndIndex(10, 0, 9) shouldEqual Some(8)
    }

    scenario("10 elements; from = 0, size = 11") {
      HistoryIndexUtil.calculateEndIndex(10, 0, 11) shouldEqual Some(9)
    }

    scenario("11 elements; from = 0, size = 10") {
      HistoryIndexUtil.calculateEndIndex(11, 0, 10) shouldEqual Some(9)
    }

    scenario("11 elements; from = 1, size = 10") {
      HistoryIndexUtil.calculateEndIndex(11, 1, 10) shouldEqual Some(10)
    }

  }

  feature("test tooling: calculateExpectedSize()") {

    scenario("beginIndex = 0; endIndex = 9") {
      HistoryIndexUtil.calculateExpectedSize(0, 9) should be(10)
    }

    scenario("beginIndex = 1; endIndex = 9") {
      HistoryIndexUtil.calculateExpectedSize(1, 9) should be(9)
    }

  }

}
