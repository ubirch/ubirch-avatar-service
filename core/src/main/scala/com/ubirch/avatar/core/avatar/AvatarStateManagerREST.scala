package com.ubirch.avatar.core.avatar

import java.util.UUID

import com.ubirch.avatar.model._
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.mongo.connection.MongoUtil

import org.joda.time.DateTime
import org.json4s.JValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-13
  */
object AvatarStateManagerREST {

  def setReported(deviceId: UUID, reported: JValue)(implicit mongo: MongoUtil): Future[Option[rest.aws.AvatarState]] = {

    // TODO integration tests
    AvatarStateManager.byDeviceId(deviceId) flatMap {

      case None =>
        val restState = rest.aws.AvatarState(
          deviceId = deviceId,
          desired = None, // TODO desired=device.deviceConfig
          reported = Some(reported)
        )
        val dbState = Json4sUtil.any2any[db.device.AvatarState](restState)
        AvatarStateManager.create(dbState) map {
          case None => None
          case Some(created: db.device.AvatarState) =>
            // TODO calculate "inSync"
            // TODO calculate "delta"
            Some(Json4sUtil.any2any[rest.aws.AvatarState](created))
        }

      case Some(existingState: db.device.AvatarState) =>

        val restState = Json4sUtil.any2any[rest.aws.AvatarState](existingState)
          .copy(
            reported = Some(reported),
            deviceLastUpdated = Some(DateTime.now)
          )
        val toUpdate = Json4sUtil.any2any[db.device.AvatarState](restState)

        AvatarStateManager.update(toUpdate) map {

          case None => None

          case Some(dbState: db.device.AvatarState) =>
            val pre = Json4sUtil.any2any[rest.aws.AvatarState](dbState)
            // TODO calculate "inSync"
            // TODO calculate "delta"
            Some(pre)

        }

    }

  }

  def setDesired(deviceId: UUID, newState: JValue)(implicit mongo: MongoUtil): Future[Option[rest.aws.AvatarState]] = {

    // TODO integration tests
    // TODO implement: see AwsShadowUtil#setDesired
    Future(None)

  }

}
