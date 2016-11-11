package com.ubirch.avatar.util.model

import com.ubirch.avatar.config.Const
import com.ubirch.avatar.model.device.{DeviceType, DeviceTypeDefaults, DeviceTypeName}
import com.ubirch.util.json.Json4sUtil

import org.json4s.JValue

import scala.collection.mutable.ListBuffer

/**
  * author: cvandrei
  * since: 2016-11-11
  */
object DeviceTypeUtil {

  val defaultKey = "defaultDeviceType"

  val defaultDeviceTypesSet: Set[String] = Set(Const.LIGHTSSENSOR, Const.LIGHTSLAMP, Const.ENVIRONMENTSENSOR)

  def dataSeries(prefix: String = defaultKey,
                 elementCount: Int = 5,
                 startIndex: Int = 0
                ): Seq[DeviceType] = {

    val series: ListBuffer[DeviceType] = ListBuffer()

    for (i <- startIndex until startIndex + elementCount) {
      val deviceType = defaultDeviceType(s"$prefix$i")
      series += deviceType
    }

    series.toSeq

  }

  def defaultDeviceTypes: Set[DeviceType] = defaultDeviceTypesSet map defaultDeviceType

  def defaultDeviceType(deviceType: String = defaultKey): DeviceType = {
    DeviceType(
      key = deviceType,
      name = defaultTranslation(deviceType),
      icon = deviceType,
      transformerQueue = Some(s"ubirch.transformer.${deviceType.toLowerCase.trim}"),
      defaults = DeviceTypeDefaults(
        defaultProps(deviceType),
        defaultConf(deviceType),
        defaultTags(deviceType)
      )
    )
  }

  def defaultTranslation(deviceType: String): DeviceTypeName = {

    deviceType match {

      case Const.LIGHTSSENSOR => DeviceTypeName("Lichtsensor", "Light Sensor")

      case Const.LIGHTSLAMP => DeviceTypeName("Lampe", "Lamp")

      case Const.ENVIRONMENTSENSOR => DeviceTypeName("Umweltsensor", "Environment Sensor")

      case _ => DeviceTypeName("Unbekanntes Gerät", "Unknown Device")

    }

  }

  def defaultProps(deviceTypeKey: String): JValue = {

    val props = deviceTypeKey match {

      case Const.LIGHTSLAMP => Map.empty

      case Const.LIGHTSSENSOR => Map(Const.STOREDATA -> Const.BOOL_TRUE)

      case Const.ENVIRONMENTSENSOR =>
        Map(
          Const.STOREDATA -> Const.BOOL_TRUE,
          Const.BLOCKC -> Const.BOOL_TRUE
        )

      case _ => Map.empty

    }

    Json4sUtil.any2jvalue(props).get

  }

  /**
    * The following fields can be included in default configs:
    *
    * * s = sensor sensitivity
    * * ir = infrared filter
    * * bf = 0/1
    * * i = update interval
    *
    * @param deviceType device type the default config applies to
    * @return default config for the given device type
    */
  def defaultConf(deviceType: String): JValue = {

    val conf = deviceType match {

      case Const.LIGHTSSENSOR =>
        Map(
          Const.CONF_INTERVALL -> (15 * 60),
          Const.CONF_SENSIVITY -> 0,
          Const.CONF_INFRARED -> 20
        )

      case Const.LIGHTSLAMP =>
        Map(
          Const.CONF_INTERVALL -> (15 * 60),
          Const.CONF_BLINKING -> 0
        )

      case Const.ENVIRONMENTSENSOR =>
        Map(
          Const.CONF_INTERVALL -> (15 * 60),
          Const.CONF_THRESHOLD -> 3600
        )

      case _ => Map(Const.CONF_INTERVALL -> (15 * 60))

    }

    Json4sUtil.any2jvalue(conf).get

  }

  def defaultTags(deviceTypeKey: String): Set[String] = {

    deviceTypeKey match {

      case Const.LIGHTSLAMP =>
        Set(
          Const.TAG_UBB0,
          Const.TAG_ACTOR,
          Const.TAG_BTCD
        )

      case Const.LIGHTSSENSOR =>
        Set(
          Const.TAG_UBB0,
          Const.TAG_SENSOR,
          Const.TAG_BTCD
        )

      case Const.ENVIRONMENTSENSOR =>
        Set(
          Const.TAG_UBB1,
          Const.TAG_SENSOR,
          Const.TAG_BTCD
        )

      case _ => Set(Const.TAG_UBB1)

    }

  }

}
