package com.ubirch.avatar.cmd

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.{Config, ConfigKeys, Const}
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.util.server.{ElasticsearchMappings, MongoConstraints}
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil

object PatchDevices extends App
  with ElasticsearchMappings
  with MongoConstraints
  with MyJsonProtocol
  with StrictLogging {

  implicit val system = ActorSystem("AvatarService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val httpClient: HttpExt = Http()

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  val adminGroup = Set(
    UUIDUtil.fromString("7ff974c8-8224-4c14-9db3-2f0ecc5ff83e"),
    UUIDUtil.fromString("116b01ab-2a13-4126-9251-e49d06639ea2"),
    UUIDUtil.fromString("8f6d9eba-d5e5-4d00-aed2-ebba9253413d"),
    UUIDUtil.fromString("05c6fb1a-5e82-419a-a6c1-3feaa991b70b")
  )

  val trackleServiceQueue = s"${Config.enviroment}-trackle-service-inbox"

  def patch = {
    DeviceManager.all(adminGroup) map {
      devices =>
        devices.filter(d => Const.TRACKLESENSOR.equals(d.deviceTypeKey)) foreach {
          device =>
            val patchedDev = device.copy(
              //              pubRawQueues = Some(device.pubRawQueues.get + trackleServiceQueue)
              pubRawQueues = Some(Set("trackle-demo-avatar-service-inbox", trackleServiceQueue))
            )
            println(patchedDev.deviceName)
            println(patchedDev.deviceTypeKey)
            println(patchedDev.pubRawQueues)
            println("--")
            DeviceManager.update(patchedDev)
        }
    }
  }

  patch
}
