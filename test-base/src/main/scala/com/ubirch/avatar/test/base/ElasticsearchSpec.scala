package com.ubirch.avatar.test.base

import com.ubirch.avatar.storage.StorageCleanup

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

/**
  * author: cvandrei
  * since: 2016-10-26
  */
trait ElasticsearchSpec extends UnitSpec
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with StorageCleanup {

  override protected def beforeEach(): Unit = {
    cleanElasticsearch()
    Thread.sleep(100)
  }

  override protected def afterAll(): Unit = {
    esClientClose()
    super.afterAll()
  }

}
