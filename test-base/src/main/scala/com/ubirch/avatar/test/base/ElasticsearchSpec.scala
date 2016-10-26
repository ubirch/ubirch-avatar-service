package com.ubirch.avatar.test.base

import com.ubirch.avatar.test.util.StorageCleanup

import org.scalatest.BeforeAndAfterEach

/**
  * author: cvandrei
  * since: 2016-10-26
  */
trait ElasticsearchSpec extends UnitSpec
  with BeforeAndAfterEach
  with StorageCleanup {

  override protected def beforeEach(): Unit = {
    resetStorage()
    Thread.sleep(100)
  }

}
