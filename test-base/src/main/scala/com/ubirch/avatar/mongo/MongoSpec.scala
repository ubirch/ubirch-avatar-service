package com.ubirch.avatar.mongo

import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.test.MongoTestUtils

import org.scalatest.{AsyncFeatureSpec, BeforeAndAfterAll, BeforeAndAfterEach, Matchers}

/**
  * author: cvandrei
  * since: 2017-06-13
  */
class MongoSpec extends AsyncFeatureSpec
  with Matchers
  with BeforeAndAfterEach
  with BeforeAndAfterAll {

  protected implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  protected val mongoTestUtils = new MongoTestUtils()

  override protected def beforeEach(): Unit = {
    mongo.db map (_.drop())
    Thread.sleep(100)
  }

  override protected def afterAll(): Unit = mongo.close()

}
