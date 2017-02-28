package com.ubirch.avatar.test.base

import com.ubirch.avatar.storage.StorageCleanup

import org.scalatest.{AsyncFeatureSpec, BeforeAndAfterEach, Matchers}

/**
  * author: cvandrei
  * since: 2017-02-28
  */
trait ElasticsearchSpecAsync extends AsyncFeatureSpec
  with Matchers
  with BeforeAndAfterEach
  with StorageCleanup {

  override protected def beforeEach(): Unit = {
    cleanElasticsearch()
    Thread.sleep(100)
  }

}
