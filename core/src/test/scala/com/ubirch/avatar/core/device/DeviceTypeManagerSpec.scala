package com.ubirch.avatar.core.device

import com.ubirch.avatar.test.base.ElasticsearchSpec
import com.ubirch.util.json.MyJsonProtocol

/**
  * author: cvandrei
  * since: 2016-11-10
  */
class DeviceTypeManagerSpec extends ElasticsearchSpec
  with MyJsonProtocol {

  feature("all()") {

    // TODO implement all (ignored) test cases

    ignore("index does not exist") {

    }

    ignore("index exists; no records exist") {

    }

    ignore("some records exist") {

    }

  }

  feature("getByKey()") {

    // TODO implement all (ignored) test cases

    ignore("index does not exist") {

    }

    ignore("index exists; no record matching the given key exist") {

    }

    ignore("record matching the given key exists") {

    }

  }

  feature("create()") {

    // TODO implement all (ignored) test cases

    ignore("index does not exist") {

    }

    ignore("index exists; no record with given key exists --> create is successful") {

    }

    ignore("record with given key exists --> create fails") {

    }

  }

  feature("update()") {

    // TODO implement all (ignored) test cases

    ignore("index does not exist") {

    }

    ignore("index exists; no record with given key exists --> update fails") {

    }

    ignore("record with given key exists --> update is successful") {

    }

  }

  feature("init()") {

    // TODO implement all (ignored) test cases
    ignore("index does not exist --> default deviceTypes are created") {

    }

    ignore("index exists; no records exist --> default deviceTypes are created") {

    }

    ignore("records exist --> no deviceTypes are created") {

    }

  }

}
