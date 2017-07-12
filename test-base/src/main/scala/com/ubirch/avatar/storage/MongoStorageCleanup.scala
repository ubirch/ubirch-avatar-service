package com.ubirch.avatar.storage

import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.avatar.util.server.MongoConstraints
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2017-07-12
  */
trait MongoStorageCleanup extends MongoConstraints {

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  final def mongoClose(): Unit = mongo.close()

  def dropDb(): Unit = {
    Await.result(mongo.db map(_.drop), 60 seconds)
    Thread.sleep(100)
    logger.info("dropped mongo database")
  }

  def cleanMongoDb(): Unit = {
    dropDb()
    createMongoConstraints()
  }

}
