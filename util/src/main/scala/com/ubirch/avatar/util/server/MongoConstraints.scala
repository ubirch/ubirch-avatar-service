package com.ubirch.avatar.util.server

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.util.mongo.MongoConstraintsBase
import reactivemongo.api.indexes.{ Index, IndexType }

/**
  * author: cvandrei
  * since: 2017-07-12
  */
trait MongoConstraints extends MongoConstraintsBase with StrictLogging {

  val constraintsToCreate: Map[String, Set[Index.Default]] = Map(
    Config.mongoCollectionAvatarState -> Set(
      Index(key = Seq(("deviceId", IndexType.Ascending)), name = Some("avatarStateUniqueDeviceId"), unique = true)
    )
  )

  val constraintsToDrop: Map[String, Set[String]] = Map.empty

  val collections: Set[String] = Set(
    Config.mongoCollectionAvatarState
  )

}
