package com.ubirch.avatar.core.avatar

import java.util.UUID

import com.ubirch.avatar.model._
import com.ubirch.avatar.model.rest.device.Device
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.mongo.connection.MongoUtil

import org.json4s.JValue
import org.json4s.jackson.JsonMethods._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-13
  */
object AvatarStateManagerREST {

  private val emptyJson = parse("{}")

  def byDeviceId(deviceId: UUID)(implicit mongo: MongoUtil): Future[Option[rest.device.AvatarState]] = {

    // TODO automated tests
    AvatarStateManager.byDeviceId(deviceId) map {

      case None => None
      case Some(dbAvatarState: db.device.AvatarState) => Some(toRestModel(dbAvatarState))

    }

  }

  def setReported(restDevice: Device, reported: JValue)(implicit mongo: MongoUtil): Future[Option[rest.device.AvatarState]] = {

    val dbDevice = Json4sUtil.any2any[db.device.Device](restDevice)
    AvatarStateManager.setReported(dbDevice, reported) map {

      case None => None
      case Some(dbAvatarState: db.device.AvatarState) => Some(toRestModel(dbAvatarState))

    }

  }

  def setDesired(restDevice: Device, desired: JValue)(implicit mongo: MongoUtil): Future[Option[rest.device.AvatarState]] = {

    val dbDevice = Json4sUtil.any2any[db.device.Device](restDevice)
    AvatarStateManager.setDesired(dbDevice, desired) map {

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
      deviceLastUpdated = dbAvatarState.deviceLastUpdated,
      avatarLastUpdated = dbAvatarState.avatarLastUpdated
    )

    val diff = restAvatarStatePrelim.reported.getOrElse(emptyJson) diff restAvatarStatePrelim.desired.getOrElse(emptyJson)
    val delta: JValue = diff.changed merge diff.added

    restAvatarStatePrelim.copy(delta = Some(delta))

  }

  private def stringToJson(s: Option[String]): Option[JValue] = {

    s match {
      case None => None
      case Some(jsonString: String) => Some(parse(jsonString))
    }

  }

}
