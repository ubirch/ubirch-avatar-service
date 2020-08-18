package com.ubirch.avatar.core.avatar

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.db.device.{AvatarState, Device}
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.deepCheck.util.DeepCheckResponseUtil
import com.ubirch.util.json.{Json4sUtil, JsonFormats}
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats
import org.joda.time.DateTime
import org.json4s.jackson.JsonMethods._
import org.json4s.native.Serialization.write
import org.json4s.{Formats, JValue}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-02-24
  */
object AvatarStateManager extends MongoFormats
  with StrictLogging {

  private implicit val formats: Formats = JsonFormats.default


  private val collectionName = Config.mongoCollectionAvatarState

  implicit protected def avatarStateWriter: BSONDocumentWriter[AvatarState] = Macros.writer[AvatarState]

  implicit protected def avatarStateReader: BSONDocumentReader[AvatarState] = Macros.reader[AvatarState]

  /**
    * Search an [[AvatarState]] based on the id of it's device.
    *
    * @param deviceId deviceId to search with
    * @return None if nothing was found
    */
  def byDeviceId(deviceId: String)(implicit mongo: MongoUtil): Future[Option[AvatarState]] = {

    logger.debug(s"query byDeviceId: deviceId=$deviceId")
    val selector = document("deviceId" -> deviceId)

    mongo.collection(collectionName) flatMap {
      _.find[BSONDocument, AvatarState](selector).one[AvatarState]
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

        logger.error(s"unable to create avatarState (as it already exists) for deviceId=${avatarState.deviceId}")
        Future(None)

      case None =>

        mongo.collection(collectionName) flatMap { collection =>

          collection.insert[AvatarState](avatarState) map { writeResult =>

            if (writeResult.ok && writeResult.n == 1) {
              logger.debug(s"created new avatarState: $avatarState")
              Some(avatarState)
            } else {
              logger.error("failed to create avatarState")
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

    val deviceId = avatarState.deviceId
    byDeviceId(deviceId) flatMap {

      case None =>
        logger.error(s"unable to update if no AvatarState exists: deviceId=$deviceId")
        Future(None)

      case Some(_: AvatarState) =>

        val selector = document("deviceId" -> avatarState.deviceId)
        mongo.collection(collectionName) flatMap {

          _.update(selector, avatarState) map { writeResult =>

            if (writeResult.ok) {
              logger.info(s"updated avatarState: deviceId=${avatarState.deviceId}")
              Some(avatarState)
            } else {
              logger.error(s"failed to update avatarState: avatarState=$avatarState; writeResult=$writeResult")
              None
            }

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

  def setReported(device: Device, reported: JValue, signature: Option[String] = None)
                 (implicit mongo: MongoUtil): Future[Option[AvatarState]] = {

    val reportedString = Some(Json4sUtil.jvalue2String(reported))
    byDeviceId(device.deviceId) flatMap {

      case None =>

        val toCreate = newAvatarStateWithReported(device, reportedString, signature = signature)
        logger.debug(s"setReported() - creating new AvatarState: $toCreate")
        create(toCreate)

      case Some(avatarState: AvatarState) =>

        val toUpdate = avatarState.copy(
          reported = reportedString,
          deviceLastUpdated = Some(DateTime.now),
          currentDeviceSignature = signature
        )
        logger.debug(s"setReported() - updating AvatarState: $toUpdate")
        update(toUpdate)

    }

  }

  def setDesired(device: Device, desired: JValue)
                (implicit mongo: MongoUtil): Future[Option[AvatarState]] = {

    val desiredString = Some(Json4sUtil.jvalue2String(desired))
    byDeviceId(device.deviceId) flatMap {

      case None =>

        val toCreate = newAvatarStateWithDesired(device, desiredString)
        create(toCreate)

      case Some(avatarState: AvatarState) =>

        val newDesired = avatarState.desired match {
          case None => Some(desired)
          case Some(currentDesired: String) => Some(parse(currentDesired) merge desired)
        }
        val toUpdate = avatarState.copy(
          desired = Some(write(newDesired)),
          avatarLastUpdated = Some(DateTime.now)
        )
        update(toUpdate)

    }

  }

  def connectivityCheck(serviceName: String)(implicit mongo: MongoUtil): Future[DeepCheckResponse] = {

    logger.debug(s"connectivityCheck($serviceName)")
    mongo.connectivityCheck[AvatarState](collectionName) map { deepCheckRes =>
      DeepCheckResponseUtil.addServicePrefix(serviceName, deepCheckRes)
    } recover[DeepCheckResponse] {
      case e =>
        logger.error("", e)
        DeepCheckResponseUtil.addServicePrefix(serviceName,
          DeepCheckResponse(status = false, messages = Seq(s"[avatar-state] ${e.getMessage}"))
        )
    }
  }

  private def newAvatarStateWithReported(device: Device, reported: Option[String], signature: Option[String] = None): AvatarState = {

    device.deviceConfig match {

      case None =>

        AvatarState(
          deviceId = device.deviceId,
          reported = reported,
          currentDeviceSignature = signature
        )

      case Some(deviceConfig: JValue) =>

        AvatarState(
          deviceId = device.deviceId,
          reported = reported,
          desired = Some(Json4sUtil.jvalue2String(deviceConfig)),
          currentDeviceSignature = signature
        )

    }

  }

  private def newAvatarStateWithDesired(device: Device, desired: Option[String]): AvatarState = {

    AvatarState(
      deviceId = device.deviceId,
      desired = desired
    )

  }

}
