package com.ubirch.avatar.util.server

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Config
import com.ubirch.util.mongo.MongoConstraintsBase

import reactivemongo.api.indexes.{Index, IndexType}

/**
  * author: cvandrei
  * since: 2017-07-12
  */
trait MongoConstraints extends MongoConstraintsBase
  with StrictLogging {

  val constraintsToCreate: Map[String, Set[Index]] = Map(
    Config.mongoCollectionAvatarState -> Set(
      Index(name = Some("avatarStateUniqueDeviceId"), key = Seq(("deviceId", IndexType.Ascending)), unique = true)
    )
  )

  val constraintsToDrop: Map[String, Set[String]] = Map.empty

  val collections: Set[String] = Set(
    Config.mongoCollectionAvatarState
  )

}
