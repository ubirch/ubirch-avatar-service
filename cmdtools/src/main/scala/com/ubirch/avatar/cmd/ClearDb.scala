package com.ubirch.avatar.cmd

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.core.device.DeviceTypeManager
import com.ubirch.avatar.storage.{ESStorageCleanup, MongoStorageCleanup}

/**
  * author: derMicha
  * since: 12/12/16
  */
object ClearDb
  extends App
    with StrictLogging
    with ESStorageCleanup
    with MongoStorageCleanup {

  logger.info("reset avatar service db: Elasticsearch")
  cleanElasticsearch()
  DeviceTypeManager.init()
  esClientClose()

  logger.info("reset avatar service db: MongoDb")
  cleanMongoDb()
  mongoClose()

}
