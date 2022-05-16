package com.ubirch.avatar.util.model

import com.ubirch.avatar.config.Const
import com.ubirch.avatar.model.rest.device.{DeviceType, DeviceTypeDefaults, DeviceTypeName}
import com.ubirch.util.json.Json4sUtil
import org.json4s.JValue

/**
  * author: cvandrei
  * since: 2016-11-11
  */
object DeviceTypeUtil {

  val defaultKey = "defaultDeviceType"


  def defaultDeviceType(deviceType: String = defaultKey): DeviceType = {
    DeviceType(
      key = deviceType,
      name = defaultTranslation,
      icon = defaultIcon,
      transformerQueue = Some(s"ubirch.transformer.${deviceType.toLowerCase.trim}"),
      displayKeys = Some(defaultDisplayKeys),
      defaults = DeviceTypeDefaults(
        defaultProps,
        defaultConf,
        defaultTags
      )
    )
  }

  val defaultTranslation: DeviceTypeName = DeviceTypeName("trackle", "trackle Sensor")

  val defaultIcon: String = "ion-radio-waves"


  val defaultProps: JValue = {

    val props =
      Map(
        Const.STOREDATA -> Const.BOOL_TRUE,
        Const.BLOCKC -> Const.BOOL_FALSE,
        Const.CHAINDATA -> Const.BOOL_FALSE,
        Const.CHAINHASHEDDATA -> Const.BOOL_FALSE,
        Const.CHECKREPLAY -> Const.BOOL_FALSE
      )

    Json4sUtil.any2jvalue(props).get

  }

  val defaultDisplayKeys: Array[String] = Array("te", "er")

  /**
    * The following fields can be included in default configs:
    *
    * * s = sensor sensitivity
    * * ir = infrared filter
    * * bf = 0/1
    * * i = update interval
    *
    * @return default config for the given device type
    */
  val defaultConf: JValue = {

    val conf =
      Map(
        Const.CONF_INTERVALL -> (1 * 60 * 1000),
        Const.CONF_INTERVALLLENGHT -> (30 * 60 * 1000),
        Const.CONF_MIN -> 35 * 100,
        Const.CONF_MAX -> 42 * 100
      )
    Json4sUtil.any2jvalue(conf).get
  }

  val defaultTags: Set[String] = {
    Set(
      Const.TAG_SENSOR,
      Const.TAG_TRACKLE,
      Const.TAG_MED,
      Const.TAG_BLE
    )
  }
}
