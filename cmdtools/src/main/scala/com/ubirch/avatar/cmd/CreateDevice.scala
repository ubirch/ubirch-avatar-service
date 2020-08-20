package com.ubirch.avatar.cmd

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.config.{ConfigKeys, Const}
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.avatar.util.server.{ElasticsearchMappings, MongoConstraints}
import com.ubirch.util.crypto.hash.HashUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil
import org.json4s.JsonAST.JValue
import org.json4s.native.Serialization.read

import scala.concurrent.ExecutionContextExecutor

object CreateDevice extends App
  with ElasticsearchMappings
  with MongoConstraints
  with MyJsonProtocol
  with StrictLogging {

  implicit val system: ActorSystem = ActorSystem("AvatarService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val httpClient: HttpExt = Http()

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  val adminGroup = UUIDUtil.fromString("7ff974c8-8224-4c14-9db3-2f0ecc5ff83e")

  val properties_NOBC: JValue = read[JValue](
    s"""
       |{
       |"${Const.BLOCKC}" : false,
       |"${Const.STOREDATA}" : true
       |}
       |""".stripMargin
  )

  val devices = Map[String, Array[String]](
    "455ed91b749dc31a-03eb75411e08cbe9" -> Array("Katharina Wienecke", "9c66de5d-04de-415b-957b-59358cce6b80"),
    "d3a7d0113f96b75d-46db9fabb4a9f818" -> Array("Maxim Loick", "1a906d10-89a7-44e7-9207-c27365b0aed1"),
    "32db34c91ae96ec8-28f01ee4724b0119" -> Array("Tanja Lübbers", "4da31348-ee6f-436d-a4fd-abd418453e0e"),
    "a363a84705c2a2df-a1dfec6ae7feb1c0" -> Array("Simone Esch", "3524e434-de16-4939-a09a-c44b062e41bb"),
    "7147ab38810a44a8-2e6de8352cb69564" -> Array("Franziska Küsters", "c79a8fad-764f-4e43-9901-73e92a6ab324"),
    "2311267489562c5c-37c5cff4f4c2dbc4" -> Array("Katrin Reuter", "3a60a4c3-2d0c-4f62-9f59-e3efea2538dc"),
    "40e4a84f37d1e224-1c8f7dfcba7c1578" -> Array("Beate Fiß", "727604d9-cbc7-4de9-9e26-15b528c8a4d4"),
    "67b0cace1340ba04-7a834b59dffe65d3" -> Array("Teresa Bücker", "2d48ab4f-24fb-4977-8853-5d9df37825e7"),
    "71f4cc0b5aeb25c4-59951d71cb50c632" -> Array("Heinke Wolf", "03b54e51-4f20-4206-984a-4861da2fb94f")
  )


  def create = {
    devices.keySet.map { k =>
      DeviceManager.infoByHwId(k).map {
        case Some(d) =>
          val devName = d.deviceName
          println(s"device exists: $devName")
        case None =>
          println("device doesn't exists")
          val v = devices.get(k).get
          val userName = v(0)
          val userId = UUIDUtil.fromString(v(1))
          println(s"$k : $userName")

          val newD = Device(
            deviceId = userId.toString,
            owners = Set(userId),
            groups = Set(adminGroup),
            deviceName = s"trackle $userName",
            hwDeviceId = k,
            deviceTypeKey = Const.TRACKLESENSOR,
            hashedHwDeviceId = HashUtil.sha512Base64(k),
            deviceProperties = Some(properties_NOBC),
            tags = Set("trackle", "sensor", "GTT")
          )
          DeviceManager.create(newD)
      }
    }
  }

  def update = {
    devices.keySet.map { k =>
      DeviceManager.infoByHwId(k).map {
        case Some(d: Device) =>
          println(d.pubQueues)
          val uD = d.copy(
            pubQueues = Some(Set[String]("trackle-demo-avatar-service-outbox"))
          )

        //DeviceManager.update(uD)
        case None =>
      }
    }
  }

  //create
  update
}
