package com.ubirch.avatar.cmd

import akka.actor.ActorSystem
import akka.http.scaladsl.{ Http, HttpExt }
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.{ Config, ConfigKeys, Const }
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.util.server.{ ElasticsearchMappings, MongoConstraints }
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContextExecutor

object PatchTrackleDevices
  extends App
  with ElasticsearchMappings
  with MongoConstraints
  with MyJsonProtocol
  with StrictLogging {

  implicit val system: ActorSystem = ActorSystem("AvatarService")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val httpClient: HttpExt = Http()

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  val adminGroup = Set(
    //    UUIDUtil.fromString(
    //      //trackle-dev admin
    //      "7ff974c8-8224-4c14-9db3-2f0ecc5ff83e"
    //    )
  )

  val trackleServiceQueue = s"${Config.enviroment}-trackle-service-inbox"

  def patch = {
    //DeviceManager.all(adminGroup) map {
    DeviceManager.all() map {
      devices =>
        devices.filter(d => Const.TRACKLESENSOR.equals(d.deviceTypeKey)) foreach { device =>
          val patchedDev = device.copy(
            pubQueues = Some(
              Set(
                //s"${Config.enviroment}_ubirch_transformer_outbox"
              )),
            pubRawQueues = Some(
              Set(
                //                s"${Config.enviroment}_ubirch_transformer_inbox",
                trackleServiceQueue
              )
            ),
            groups = device.groups ++ adminGroup
          )
          val queueNames = patchedDev.pubRawQueues.getOrElse(Set.empty).mkString(",")
          logger.info(s"${patchedDev.deviceName} \t\t ${patchedDev.deviceTypeKey} \t\t $queueNames")

          Thread.sleep(250)
          DeviceManager.update(patchedDev)
        }

    }
  }

  patch
}
