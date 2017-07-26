package com.ubirch.avatar.test.base

import com.ubirch.avatar.storage.ESStorageCleanup

import org.scalatest.{AsyncFeatureSpec, BeforeAndAfterAll, BeforeAndAfterEach, Matchers}

/**
  * author: cvandrei
  * since: 2017-02-28
  */
trait ElasticsearchSpecAsync extends AsyncFeatureSpec
  with Matchers
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with ESStorageCleanup {

  override protected def beforeEach(): Unit = {
    cleanElasticsearch()
    Thread.sleep(100)
  }

}
