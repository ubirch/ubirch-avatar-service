package com.ubirch.avatar.core.avatar

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.aws.AvatarState
import com.ubirch.util.elasticsearch.client.binary.storage.{ESBulkStorage, ESSimpleStorage}
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-02-24
  */
object AvatarStateManager extends MyJsonProtocol
  with StrictLogging {

  private val index = Config.esAvatarStateIndex
  private val esType = Config.esAvatarStateType

  /**
    * Search an [[AvatarState]] based on the id of it's device.
    *
    * @param deviceId deviceId to search with
    * @return None if nothing was found
    */
  def byDeviceId(deviceId: UUID): Future[Option[AvatarState]] = {

    logger.debug(s"query byDeviceId: deviceId=$deviceId")

    ESSimpleStorage.getDoc(
      docIndex = index,
      docType = esType,
      docId = deviceId.toString
    ) map {
      case Some(res) => Some(res.extract[AvatarState])
      case None => None
    }

  }

  /**
    * Create a [[AvatarState]].
    *
    * @param avatarState object to store
    * @return stored entity; None if something went wrong or entity already exists
    */
  def create(avatarState: AvatarState): Future[Option[AvatarState]] = {

    logger.debug(s"create avatarState: $avatarState")
    byDeviceId(avatarState.deviceId) flatMap {
      case Some(_) => Future(None)
      case None => upsert(avatarState)
    }

  }

  /**
    * Updates a [[AvatarState]].
    *
    * @param avatarState object to update
    * @return updated object; None if something went wrong
    */
  def update(avatarState: AvatarState): Future[Option[AvatarState]] = {

    logger.debug(s"update avatarState: $avatarState")

    byDeviceId(avatarState.deviceId) flatMap {
      case Some(_) => upsert(avatarState)
      case None => Future(None)
    }

  }

  /**
    * Insert or update an [[AvatarState]].
    *
    * @param state object to upsert
    * @return json of what we stored; None if something went wrong
    */
  def upsert(state: AvatarState): Future[Option[AvatarState]] = {

    Json4sUtil.any2jvalue(state) match {

      case Some(doc) =>

        val id = state.deviceId.toString
        ESBulkStorage.storeDocBulk(
          docIndex = index,
          docType = esType,
          docId = id,
          doc = doc
        ) map (_.extractOpt[AvatarState])

      case None => Future(None)

    }

  }

}
