package com.ubirch.avatar.mongo

import com.ubirch.avatar.storage.MongoStorageCleanup
import com.ubirch.util.mongo.test.MongoTestUtils
import org.scalatest.{AsyncFeatureSpec, BeforeAndAfterAll, BeforeAndAfterEach, Matchers}

/**
  * author: cvandrei
  * since: 2017-06-13
  */
class MongoSpec extends AsyncFeatureSpec
  with Matchers
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with MongoStorageCleanup {

  protected val mongoTestUtils = new MongoTestUtils()

  override protected def beforeEach(): Unit = {
    cleanMongoDb()
  }

}
