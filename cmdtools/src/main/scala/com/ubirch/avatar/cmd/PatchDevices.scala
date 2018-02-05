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
    UUIDUtil.fromString("116b01ab-2a13-4126-9251-e49d06639ea2")
  )

  val trackleServiceQueue =  s"${Config.enviroment}-trackle-service-inbox"

  def patch = {
    DeviceManager.all(adminGroup) map {
      devices =>
        devices.filter(d => Const.TRACKLESENSOR.equals(d.deviceTypeKey))foreach {
          device =>
            println(device.deviceName)
            println(device.deviceTypeKey)
            println(device.pubRawQueues)
            println("--")
            val patchedDev = device.copy(
              pubRawQueues = Some(device.pubRawQueues.get + trackleServiceQueue)
              //pubRawQueues = Some(Set("local_dev_ubirch_transformer_inbox", trackleServiceQueue))
            )
            DeviceManager.update(patchedDev)
        }
    }
  }
  patch
}
