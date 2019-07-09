package com.ubirch.avatar.cmd

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.{Config, ConfigKeys}
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.util.server.{ElasticsearchMappings, MongoConstraints}
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.ExecutionContextExecutor

object PatchDevices extends App
  with ElasticsearchMappings
  with MongoConstraints
  with MyJsonProtocol
  with StrictLogging {

  implicit val system: ActorSystem = ActorSystem("AvatarService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val httpClient: HttpExt = Http()

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  val adminGroup = Set(
    UUIDUtil.fromString("db1488ae-becc-40a3-a5c2-b6daadd6715b")
  )

  val ubirchServiceQueue = s"ubirch-${Config.enviroment}_ubirch_transformer_inbox"

  def patch = {
    //    DeviceManager.all(adminGroup) map {
    DeviceManager.all() map {
      devices =>

        devices.foreach { device =>
          val patchedDev = device.copy(
            pubRawQueues = Some(
              Set(ubirchServiceQueue)
              //            groups = device.groups ++ adminGroup
            )
          )
          logger.info(patchedDev.deviceName)
          logger.info(patchedDev.deviceTypeKey)
          logger.info(patchedDev.pubRawQueues.getOrElse(Set.empty).mkString("'"))

          //DeviceManager.update(patchedDev)
          Thread.sleep(500)
          patchedDev
        }
    }
  }

  patch
}
