package com.ubirch.avatar.core.avatar

import java.util.UUID

import com.ubirch.avatar.model._
import com.ubirch.avatar.model.rest.device.Device
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.mongo.connection.MongoUtil

import org.json4s.JValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-13
  */
object AvatarStateManagerREST {

  def setReported(restDevice: Device, reported: JValue)(implicit mongo: MongoUtil): Future[Option[rest.aws.AvatarState]] = {

    // TODO integration tests
    val dbDevice = Json4sUtil.any2any[db.device.Device](restDevice)
    AvatarStateManager.setReported(dbDevice, reported) map {

      case None => None
      case Some(dbAvatarState: db.device.AvatarState) => Some(toRestModel(dbAvatarState))

    }

  }

  def setDesired(deviceId: UUID, newState: JValue)(implicit mongo: MongoUtil): Future[Option[rest.aws.AvatarState]] = {

    // TODO integration tests
    // TODO implement: see AwsShadowUtil#setDesired
    Future(None)

  }

  def toRestModel(dbAvatarState: db.device.AvatarState): rest.aws.AvatarState = {

    // TODO calculate "inSync"
    // TODO calculate "delta"
    Json4sUtil.any2any[rest.aws.AvatarState](dbAvatarState)

  }

}
