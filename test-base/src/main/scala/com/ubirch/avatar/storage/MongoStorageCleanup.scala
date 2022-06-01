package com.ubirch.avatar.storage

import com.ubirch.avatar.config.ConfigKeys
import com.ubirch.avatar.util.server.MongoConstraints
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2017-07-12
  */
trait MongoStorageCleanup extends MongoConstraints {

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  final def mongoClose(): Unit = mongo.close()

  final def cleanMongoDb(): Unit = {
    val r = Future.sequence(collections.map(c => mongo.collection(c).flatMap(_.drop(failIfNotFound = false))))
    Await.result(r, 30.seconds)
    createMongoConstraints()
    logger.info(s"dropped mongo documents")
  }

}
