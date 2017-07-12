package com.ubirch.avatar.cmd

import com.typesafe.scalalogging.slf4j.StrictLogging

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
  esClientClose()

  logger.info("reset avatar service db: MongoDb")
  cleanMongoDb()
  mongoClose()

}
