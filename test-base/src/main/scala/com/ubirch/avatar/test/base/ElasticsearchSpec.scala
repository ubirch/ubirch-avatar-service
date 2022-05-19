package com.ubirch.avatar.test.base

import com.ubirch.avatar.storage.ESStorageCleanup

import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }

/**
  * author: cvandrei
  * since: 2016-10-26
  */
trait ElasticsearchSpec extends UnitSpec with BeforeAndAfterEach with BeforeAndAfterAll with ESStorageCleanup {

  override protected def beforeEach(): Unit = {
    cleanElasticsearch()
    Thread.sleep(100)
  }

}
