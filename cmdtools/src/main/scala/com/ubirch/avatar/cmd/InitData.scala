package com.ubirch.avatar.cmd

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.avatar.client.AvatarServiceClient
import com.ubirch.avatar.client.model.DeviceStateUpdate
import com.ubirch.avatar.config.Const
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.model.rest.device.DeviceDataRawConverter
import com.ubirch.avatar.model.{DummyDeviceDataRaw, DummyDevices}
import com.ubirch.util.json.MyJsonProtocol
import org.json4s.JValue
import org.json4s.native.Serialization.read

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by derMicha on 09/11/16.
  */
object InitData
  extends App
    with MyJsonProtocol
    with StrictLogging {

  val numberOfRawMessages = 10
  implicit val system: ActorSystem = ActorSystem("trackleService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val httpClient: HttpExt = Http()



  val properties_BC: JValue = read[JValue](
    s"""
       |{
       |"${Const.BLOCKC}" : true,
       |"${Const.STOREDATA}" : true
       |}
       |""".stripMargin
  )

  val properties_NOBC: JValue = read[JValue](
    s"""
       |{
       |"${Const.BLOCKC}" : false,
       |"${Const.STOREDATA}" : true
       |}
       |""".stripMargin
  )

  val device = DummyDevices.device(
    deviceTypeKey = Const.ENVIRONMENTSENSOR,
    deviceProperties = Some(properties_NOBC)
  )

  Await.result(DeviceManager.create(device), 5 seconds) match {
    case Some(dev) =>

      logger.info(s"created: $dev")

      Thread.sleep(5000)

      val (_, series) = DummyDeviceDataRaw.dataSeries(
        device = device,
        elementCount = numberOfRawMessages,
        intervalMillis = 1000 * 60 * 5, // 5 mins
        timestampOffset = 0
      )

      series foreach { dataRaw =>
        logger.debug("-----------------------------------------------------------------------------------------")
        try {
          AvatarServiceClient.deviceUpdatePOST(DeviceDataRawConverter.toClientDeviceDataRaw(dataRaw)).map {
            case Right(resp: DeviceStateUpdate) =>
              logger.debug(s"response: $resp")
            case Left(jsonErrorResponse) =>
              logger.error(s"response: $jsonErrorResponse")
          }


        }
        catch {
          case e: Exception =>
            logger.error("post failed")
        }

        Thread.sleep(500)
      }

    case None =>
      logger.error("device could not be created")
  }
}
