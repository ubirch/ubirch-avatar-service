package com.ubirch.avatar.core.avatar

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.db.device.AvatarState
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats

import org.joda.time.DateTime
import org.json4s.JValue

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-02-24
  */
object AvatarStateManager extends MongoFormats
  with StrictLogging {

  private val collectionName = Config.mongoCollectionAvatarState

  implicit protected def avatarStateWriter: BSONDocumentWriter[AvatarState] = Macros.writer[AvatarState]

  implicit protected def avatarStateReader: BSONDocumentReader[AvatarState] = Macros.reader[AvatarState]

  /**
    * Search an [[AvatarState]] based on the id of it's device.
    *
    * @param deviceId deviceId to search with
    * @return None if nothing was found
    */
  def byDeviceId(deviceId: UUID)(implicit mongo: MongoUtil): Future[Option[AvatarState]] = {

    logger.debug(s"query byDeviceId: deviceId=$deviceId")
    val selector = document("deviceId" -> deviceId)

    mongo.collection(collectionName) flatMap {
      _.find(selector).one[AvatarState]
    }

  }

  /**
    * Create a [[AvatarState]].
    *
    * @param avatarState object to store
    * @return stored entity; None if something went wrong or entity already exists
    */
  def create(avatarState: AvatarState)(implicit mongo: MongoUtil): Future[Option[AvatarState]] = {

    byDeviceId(avatarState.deviceId) flatMap {

      case Some(_: AvatarState) =>

        logger.error(s"unable to create avatarState for deviceId=${avatarState.deviceId}")
        Future(None)

      case None =>

        mongo.collection(collectionName) flatMap { collection =>

          collection.insert[AvatarState](avatarState) map { writeResult =>

            if (writeResult.ok && writeResult.n == 1) {
              logger.debug(s"created new avatarState: $avatarState")
              Some(avatarState)
            } else {
              logger.error("failed to create user")
              None
            }

          }

        }

    }

  }

  /**
    * Updates a [[AvatarState]].
    *
    * @param avatarState object to update
    * @return updated object; None if something went wrong
    */
  def update(avatarState: AvatarState)(implicit mongo: MongoUtil): Future[Option[AvatarState]] = {

    val selector = document("deviceId" -> avatarState.deviceId)
    mongo.collection(collectionName) flatMap {

      _.update(selector, avatarState) map { writeResult =>

        if (writeResult.ok && writeResult.n == 1) {
          logger.info(s"updated avatarState: deviceId=${avatarState.deviceId}")
          Some(avatarState)
        } else {
          logger.error(s"failed to update avatarState: avatarState=$avatarState")
          None
        }

      }

    }

  }

  /**
    * Insert or update an [[AvatarState]].
    *
    * @param state object to upsert
    * @return json of what we stored; None if something went wrong
    */
  def upsert(state: AvatarState)(implicit mongo: MongoUtil): Future[Option[AvatarState]] = {

    byDeviceId(state.deviceId) flatMap {
      case None => create(state)
      case Some(_: AvatarState) => update(state)
    }

  }

  def setReported(deviceId: UUID, reported: JValue)
                 (implicit mongo: MongoUtil): Future[Option[AvatarState]] = {

    // TODO automated tests
    val reportedString = Some(Json4sUtil.jvalue2String(reported))
    byDeviceId(deviceId) flatMap {

      case None =>
        val toCreate = AvatarState(
          deviceId = deviceId,
          reported = reportedString,
          desired = None // TODO desired=device.deviceConfig
        )
        create(toCreate)

      case Some(avatarState: AvatarState) =>

        val toUpdate = avatarState.copy(
          reported = reportedString,
          deviceLastUpdated = Some(DateTime.now)
        )
        update(toUpdate)

    }

  }

  def setDesired(deviceId: UUID, desired: JValue)
                (implicit mongo: MongoUtil): Future[Option[AvatarState]] = {
    // TODO implement
    Future(None)
  }

}
