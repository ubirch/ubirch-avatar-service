package com.ubirch.avatar.core.kafka


import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.ubirch.avatar.config.Config
import com.ubirch.avatar.core.device.DeviceManager
import com.ubirch.avatar.core.kafka.util.{EndOfLifeUpdate, InvalidDataException, UnexpectedException}
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.mongo.connection.MongoUtil
import org.json4s.JObject
import org.json4s.JsonAST.JField
import org.json4s.JsonDSL._

import scala.concurrent.{ExecutionContext, Future}

class EndOfLifeConsumer(actorSystem: ActorSystem)(implicit mat: Materializer, httpExt: HttpExt, mongo: MongoUtil)
  extends KafkaConsumer(Set(Config.kafkaEndOfLifeTopic), Config.kafkaEndOfLifeGroup, actorSystem) {

  private val log = Logger.apply(this.getClass.getName)

  protected def handleMessage(message: String)(implicit ec: ExecutionContext): Future[Unit] = {
    log.info("proceed EndOfLifeUpdate from kafka.")

    Json4sUtil.string2JValue(message) match {
      case Some(dataJval) =>
        dataJval.extractOpt[EndOfLifeUpdate] match {
          case Some(eol) => updateEOLConfig(eol)
          case None => Future.failed(InvalidDataException(s"can't parse message as EndOfLifeUpdate: $dataJval"))
        }
      case None => Future.failed(InvalidDataException(s"message is not json format. $message"))
    }
  }

  /**
    * This method parses the configuration of a device, updates the EOL flag if necessary and
    * updates the device accordingly in the database.
    */
  def updateEOLConfig(eol: EndOfLifeUpdate)(implicit ec: ExecutionContext): Future[Unit] = {

    DeviceManager.infoByHwId(eol.hwDeviceId).map {

      case None => throw InvalidDataException(s"couldn't find device to execute EOL update $eol.")

      case Some(device: Device) if device.deviceConfig.isEmpty =>
        throw InvalidDataException(s"EOL update not possible as device config is empty for device $device")

      case Some(device: Device) =>

        val configOpt = retrieveConfigWithoutEOLifUpdateNeeded(eol, device)

        if (configOpt.isEmpty) {
          log.info(s"EOL update not necessary as config already contains 'EOL=${eol.eolReached}'")
        } else {

          val updatedDevice = addEOLtoDeviceConfig(eol, device, configOpt)
          DeviceManager.update(updatedDevice).map {
            case Some(device) =>
              log.info(s"successfully updated EOL for device with hwDeviceId ${device.hwDeviceId}")
            case None =>
              throw UnexpectedException(s"EOL update went wrong when storing updated device $updatedDevice")
          }
        }
    }
  }

  private def addEOLtoDeviceConfig(eol: EndOfLifeUpdate, device: Device, configOpt: Option[JObject]) = {
    val newDeviceConfig = configOpt.get ~ ("EOL" -> eol.eolReached)
    val updatedDevice = device.copy(deviceConfig = Some(newDeviceConfig))
    updatedDevice
  }

  private def retrieveConfigWithoutEOLifUpdateNeeded(eol: EndOfLifeUpdate, device: Device) = {
    val oldDeviceConfig = device.deviceConfig.get.asInstanceOf[JObject]
    oldDeviceConfig.findField {
      case JField("EOL", _) => true
      case _ => false
    } match {
      case Some(field) if field._2.extract[Boolean] == eol.eolReached => None
      case Some(field) => Some(oldDeviceConfig.removeField(_ == field).asInstanceOf[JObject])
      case _ => Some(oldDeviceConfig)
    }
  }
}
