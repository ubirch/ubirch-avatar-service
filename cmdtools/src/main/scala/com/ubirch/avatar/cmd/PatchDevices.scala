package com.ubirch.avatar.cmd

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.avatar.config.ConfigKeys
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
    //UUIDUtil.fromString("7ff974c8-8224-4c14-9db3-2f0ecc5ff83e")
    UUIDUtil.fromString("db1488ae-becc-40a3-a5c2-b6daadd6715b")
  )

  //val trackleServiceQueue = s"${Config.enviroment}-trackle-service-inbox"

  def patch = {
    DeviceManager.all(Set(UUIDUtil.fromString("f4519ef9-3107-4d48-86d8-3ec5e01d815a"))) map {
      devices =>
        //devices.filter(d => Const.TRACKLESENSOR.equals(d.deviceTypeKey)) foreach {
        //devices.filter(d => d.groups.is) foreach {
        devices.foreach {
          device =>
            val patchedDev = device.copy(
              //              pubRawQueues = Some(device.pubRawQueues.get + trackleServiceQueue)
              //pubRawQueues = Some(Set("ubirch-demo-inbox"))
              pubRawQueues = Some(Set("ubirch-demo_ubirch_transformer_inbox"))
              //,groups = device.groups ++ adminGroup
            )
            logger.info(patchedDev.deviceName)
            logger.info(patchedDev.deviceTypeKey)
            logger.info(patchedDev.pubRawQueues.getOrElse(Set.empty).mkString("'"))

            DeviceManager.updateWithOutChecks(patchedDev)
        }
    }
  }

  patch
}
