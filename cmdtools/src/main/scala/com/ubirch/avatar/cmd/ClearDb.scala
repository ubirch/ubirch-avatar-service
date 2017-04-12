package com.ubirch.avatar.cmd

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.storage.StorageCleanup

/**
  * author: derMicha
  * since: 12/12/16
  */
object ClearDb
  extends App
    with StrictLogging
    with StorageCleanup {

  logger.info("reset avatar service db")
  cleanElasticsearch()
  esClientClose()

}
