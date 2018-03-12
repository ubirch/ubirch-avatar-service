package com.ubirch.avatar.util.model

import com.ubirch.avatar.config.Const
import com.ubirch.avatar.model.rest.device.{DeviceType, DeviceTypeDefaults, DeviceTypeName}
import com.ubirch.util.json.Json4sUtil
import org.json4s.JValue

import scala.collection.mutable.ListBuffer

/**
  * author: cvandrei
  * since: 2016-11-11
  */
object DeviceTypeUtil {

  val defaultKey = "defaultDeviceType"

  val defaultDeviceTypesSet: Set[String] = Set(Const.CALLIOPEMINI, Const.LIGHTSSENSOR, Const.LIGHTSLAMP, Const.ENVIRONMENTSENSOR, Const.AQSENSOR, Const.EMOSENSOR, Const.TRACKLESENSOR, Const.GENERICSENSOR, Const.UNKNOWN_DEVICE
  )

  def dataSeries(prefix: String = defaultKey,
                 elementCount: Int = 5,
                 startIndex: Int = 0
                ): Set[DeviceType] = {

    val series: ListBuffer[DeviceType] = ListBuffer()

    for (i <- startIndex until startIndex + elementCount) {
      val deviceType = defaultDeviceType(s"$prefix$i")
      series += deviceType
    }

    series.toSet

  }

  def defaultDeviceTypes: Set[DeviceType] = defaultDeviceTypesSet map defaultDeviceType

  def defaultDeviceType(deviceType: String = defaultKey): DeviceType = {
    DeviceType(
      key = deviceType,
      name = defaultTranslation(deviceType),
      icon = defaultIcon(deviceType),
      transformerQueue = Some(s"ubirch.transformer.${deviceType.toLowerCase.trim}"),
      displayKeys = Some(defaultDisplayKeys(deviceType)),
      defaults = DeviceTypeDefaults(
        defaultProps(deviceType),
        defaultConf(deviceType),
        defaultTags(deviceType)
      )
    )
  }

  def defaultTranslation(deviceType: String): DeviceTypeName = {

    deviceType match {

      case Const.CALLIOPEMINI => DeviceTypeName("Calliope mini", "Calliope mini")

      case Const.LIGHTSSENSOR => DeviceTypeName("Lichtsensor", "Light Sensor")

      case Const.LIGHTSLAMP => DeviceTypeName("Lampe", "Lamp")

      case Const.ENVIRONMENTSENSOR => DeviceTypeName("Umweltsensor", "Environment Sensor")

      case Const.AQSENSOR => DeviceTypeName("Luftqualitätsensor", "Airquality Sensor")

      case Const.EMOSENSOR => DeviceTypeName("Emotionssensor", "Emotion Sensor")

      case Const.TRACKLESENSOR => DeviceTypeName("trackle", "trackle Sensor")

      case Const.GENERICSENSOR => DeviceTypeName("ubirchSensor", "ubirch Sensor")

      case Const.UNKNOWN_DEVICE =>
        DeviceTypeName("Unbekanntes Gerät", "Unknown Device")

      case _ =>
        DeviceTypeName("Unbekanntes Gerät", "Unknown Device")

    }

  }

  def defaultIcon(deviceType: String): String = {

    deviceType match {

      case Const.CALLIOPEMINI => "ion-flash"
      case Const.LIGHTSSENSOR => "ion-ios-sunny"
      case Const.LIGHTSLAMP => "ion-ios-lightbulb"
      case Const.ENVIRONMENTSENSOR => "ion-speedometer"
      case Const.TRACKLESENSOR => "ion-radio-waves"
      case Const.AQSENSOR => "ion-ios-cloud-outline"
      case Const.EMOSENSOR => "ion-ios-pulse"
      case Const.GENERICSENSOR => "ion-radio-waves"
      case Const.UNKNOWN_DEVICE => "ion-radio-waves"
      case _ => "ion-radio-waves"

    }

  }

  def defaultProps(deviceTypeKey: String): JValue = {

    val props = deviceTypeKey match {

      case Const.CALLIOPEMINI =>
        Map(
          Const.STOREDATA -> Const.BOOL_TRUE
        )

      case Const.LIGHTSLAMP =>
        Map(
          Const.STOREDATA -> Const.BOOL_TRUE,
          Const.BLOCKC -> Const.BOOL_FALSE,
          Const.CHAINDATA -> Const.BOOL_FALSE,
          Const.CHAINHASHEDDATA -> Const.BOOL_FALSE
        )

      case Const.LIGHTSSENSOR =>
        Map(
          Const.STOREDATA -> Const.BOOL_TRUE,
          Const.BLOCKC -> Const.BOOL_FALSE,
          Const.CHAINDATA -> Const.BOOL_FALSE,
          Const.CHAINHASHEDDATA -> Const.BOOL_FALSE
        )

      case Const.ENVIRONMENTSENSOR =>
        Map(
          Const.STOREDATA -> Const.BOOL_TRUE,
          Const.BLOCKC -> Const.BOOL_FALSE,
          Const.CHAINDATA -> Const.BOOL_FALSE,
          Const.CHAINHASHEDDATA -> Const.BOOL_FALSE
        )

      case Const.AQSENSOR =>
        Map(
          Const.STOREDATA -> Const.BOOL_TRUE,
          Const.BLOCKC -> Const.BOOL_FALSE,
          Const.CHAINDATA -> Const.BOOL_FALSE,
          Const.CHAINHASHEDDATA -> Const.BOOL_FALSE
        )

      case Const.TRACKLESENSOR =>
        Map(
          Const.STOREDATA -> Const.BOOL_TRUE,
          Const.BLOCKC -> Const.BOOL_FALSE,
          Const.CHAINDATA -> Const.BOOL_FALSE,
          Const.CHAINHASHEDDATA -> Const.BOOL_FALSE
        )

      case Const.EMOSENSOR =>
        Map(
          Const.STOREDATA -> Const.BOOL_TRUE,
          Const.BLOCKC -> Const.BOOL_FALSE,
          Const.CHAINDATA -> Const.BOOL_FALSE,
          Const.CHAINHASHEDDATA -> Const.BOOL_FALSE
        )

      case Const.GENERICSENSOR =>
        Map(
          Const.STOREDATA -> Const.BOOL_TRUE,
          Const.BLOCKC -> Const.BOOL_FALSE,
          Const.CHAINDATA -> Const.BOOL_FALSE,
          Const.CHAINHASHEDDATA -> Const.BOOL_FALSE
        )

      case Const.UNKNOWN_DEVICE => Map(
        Const.STOREDATA -> Const.BOOL_TRUE,
        Const.BLOCKC -> Const.BOOL_FALSE,
        Const.CHAINDATA -> Const.BOOL_FALSE,
        Const.CHAINHASHEDDATA -> Const.BOOL_FALSE
      )

      case _ => Map.empty

    }

    Json4sUtil.any2jvalue(props).get

  }

  def defaultDisplayKeys(deviceTypeKey: String): Array[String] = {
    deviceTypeKey match {
      case Const.ENVIRONMENTSENSOR =>
        Array("temperature", "presure", "humidity", "altitude", "batteryLevel")
      case Const.AQSENSOR =>
        Array("airquality", "temperature", "presure", "humidity", "altitude", "batteryLevel")
      case Const.EMOSENSOR =>
        Array("temperature", "emg", "gsr", "pulse", "activity", "batteryLevel")
      case Const.LIGHTSSENSOR =>
        Array("r", "g", "b", "ba")
      case Const.TRACKLESENSOR =>
        Array("te", "er")
      case _ => Array()
    }
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
          Const.CONF_INTERVALL -> (10 * 60),
          Const.CONF_INTERVALLMEASSURE -> 60,
          Const.CONF_THRESHOLD -> 0
        )

      case Const.TRACKLESENSOR =>
        Map(
          Const.CONF_INTERVALL -> (1 * 60 * 1000),
          Const.CONF_INTERVALLLENGHT -> (30 * 60 * 1000),
          Const.CONF_MIN -> 35 * 100,
          Const.CONF_MAX -> 42 * 100
        )

      case Const.AQSENSOR =>
        Map(
          Const.CONF_INTERVALL -> (15 * 60),
          Const.CONF_THRESHOLD -> 0
        )

      case Const.EMOSENSOR =>
        Map(
          Const.CONF_INTERVALL -> (1 * 60)
        )

      case Const.GENERICSENSOR => Map(
        Const.CONF_INTERVALL -> (15 * 60)
      )

      case Const.UNKNOWN_DEVICE => Map(
        Const.CONF_INTERVALL -> (15 * 60)
      )

      case _ => Map(Const.CONF_INTERVALL -> (15 * 60))

    }

    Json4sUtil.any2jvalue(conf).get

  }

  def defaultTags(deviceTypeKey: String): Set[String] = {

    deviceTypeKey match {

      case Const.CALLIOPEMINI =>
        Set(
          Const.TAG_CALLIOPE
        )

      case Const.LIGHTSSENSOR =>
        Set(
          Const.TAG_UBB0,
          Const.TAG_SENSOR
        )

      case Const.ENVIRONMENTSENSOR =>
        Set(
          Const.TAG_UBB1,
          Const.TAG_SENSOR,
          Const.TAG_BTCD
        )

      case Const.AQSENSOR =>
        Set(
          Const.TAG_UBB1,
          Const.TAG_SENSOR,
          Const.TAG_BTCD
        )

      case Const.GENERICSENSOR =>
        Set(
          Const.TAG_UBB1,
          Const.TAG_SENSOR
        )

      case Const.TRACKLESENSOR => Set(
        Const.TAG_SENSOR,
        Const.TAG_TRACKLE,
        Const.TAG_MED,
        Const.TAG_BLE
      )

      case Const.UNKNOWN_DEVICE => Set(
        Const.TAG_SENSOR
      )

      case _ => Set(Const.TAG_UBB1)

    }
  }
}
