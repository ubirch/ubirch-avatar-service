package com.ubirch.avatar.core.device

import com.ubirch.avatar.test.base.ElasticsearchSpec
import com.ubirch.util.json.MyJsonProtocol

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-11-10
  */
class DeviceTypeManagerSpec extends ElasticsearchSpec
  with MyJsonProtocol {

  feature("all()") {

    scenario("index does not exist") {
      Await.result(DeviceTypeManager.all(), 1 second) should be('isEmpty)
    }

    ignore("index exists; no records exist") {
      // TODO implement test
    }

    ignore("some records exist") {
      // TODO implement test
    }

  }

  feature("getByKey()") {

    scenario("index does not exist") {
      Await.result(DeviceTypeManager.getByKey("unknownDevice"), 1 second) should be(None)
    }

    ignore("index exists; no record matching the given key exist") {
      // TODO implement test
    }

    ignore("record matching the given key exists") {
      // TODO implement test
    }

  }

  feature("create()") {

    ignore("index does not exist") {
      // TODO implement test
    }

    ignore("index exists; no record with given key exists --> create is successful") {
      // TODO implement test
    }

    ignore("record with given key exists --> create fails") {
      // TODO implement test
    }

  }

  feature("update()") {

    ignore("index does not exist") {
      // TODO implement test
    }

    ignore("index exists; no record with given key exists --> update fails") {
      // TODO implement test
    }

    ignore("record with given key exists --> update is successful") {
      // TODO implement test
    }

  }

  feature("init()") {

    ignore("index does not exist --> default deviceTypes are created") {
      // TODO implement test
    }

    ignore("index exists; no records exist --> default deviceTypes are created") {
      // TODO implement test
    }

    ignore("records exist --> no deviceTypes are created") {
      // TODO implement test
    }

  }

}
