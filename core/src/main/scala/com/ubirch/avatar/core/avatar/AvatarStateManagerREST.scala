package com.ubirch.avatar.core.avatar

import com.ubirch.avatar.model._
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.mongo.connection.MongoUtil
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-13
  */
object AvatarStateManagerREST {

  private val emptyJson = parse("{}")

  def byDeviceId(deviceId: String)(implicit mongo: MongoUtil): Future[Option[rest.device.AvatarState]] = {

    // TODO automated tests
    AvatarStateManager.byDeviceId(deviceId) map {

      case None => None
      case Some(dbAvatarState: db.device.AvatarState) => Some(toRestModel(dbAvatarState))

    }

  }

  def setReported(restDevice: Device, reported: JValue, signature: Option[String] = None)(implicit mongo: MongoUtil): Future[Option[rest.device.AvatarState]] = {

    AvatarStateManager.setReported(restDevice, reported, signature) map {

      case None => None
      case Some(dbAvatarState: db.device.AvatarState) => Some(toRestModel(dbAvatarState))

    }

  }


  def toRestModel(dbAvatarState: db.device.AvatarState): rest.device.AvatarState = {

    val desiredJvalue = stringToJson(dbAvatarState.desired)
    val reportedJvalue = stringToJson(dbAvatarState.reported)
    val restAvatarStatePrelim = rest.device.AvatarState(
      deviceId = dbAvatarState.deviceId,
      inSync = Some(dbAvatarState.reported == dbAvatarState.desired),
      desired = desiredJvalue,
      reported = reportedJvalue,
      currentDeviceSignature = dbAvatarState.currentDeviceSignature,
      deviceLastUpdated = dbAvatarState.deviceLastUpdated,
      avatarLastUpdated = dbAvatarState.avatarLastUpdated
    )

    val diff = restAvatarStatePrelim.reported.getOrElse(emptyJson) diff restAvatarStatePrelim.desired.getOrElse(emptyJson)
    val delta: JValue = diff.changed merge diff.added match {
      case jn if jn.equals(JsonAST.JNothing) =>
        //TODO ugly way to create an empty Json object
        Json4sUtil.string2JValue("{}").get
      case jn => jn
    }

    restAvatarStatePrelim.copy(delta = Some(delta))

  }

  private def stringToJson(s: Option[String]): Option[JValue] = {

    s match {
      case None => None
      case Some(jsonString: String) => Some(parse(jsonString))
    }

  }

}
