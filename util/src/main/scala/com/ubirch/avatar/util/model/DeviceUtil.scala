package com.ubirch.avatar.util.model

import java.util.UUID

import com.ubirch.avatar.model.db.device.Device
import com.ubirch.crypto.ecc.EccUtil
import com.ubirch.crypto.hash.HashUtil
import com.ubirch.server.util.ServerKeys
import com.ubirch.util.json.JsonFormats
import org.json4s._
import org.json4s.native.Serialization._

/**
  * author: derMicha
  * since: 2016-11-02
  */
object DeviceUtil {

  implicit val formats: Formats = JsonFormats.default


  def hashHwDeviceId(hwDeviceId: UUID): String = hashHwDeviceId(hwDeviceId.toString)

  def hashHwDeviceId(hwDeviceId: String): String = {
    val hwid = hwDeviceId.trim.toLowerCase
    HashUtil.sha512Base64(hwid)
  }

  /**
    *
    * @param payload as JValue
    * @return signed payloaded
    */
  def sign(payload: JValue): String = {
    val payloadStr = write(payload)
    EccUtil.signPayload(ServerKeys.privKeyB64, payloadStr)
  }

  def deviceWithDefaults(device: Device): Device = {

    // TODO automated tests
    device.copy(
      hashedHwDeviceId = HashUtil.sha512Base64(device.hwDeviceId.toLowerCase),
      hwDeviceId = device.hwDeviceId.toLowerCase,
      deviceProperties = Some(device.deviceProperties.getOrElse(
        DeviceTypeUtil.defaultProps(device.deviceTypeKey)
      )),
      deviceConfig = Some(device.deviceConfig.getOrElse(
        DeviceTypeUtil.defaultConf(device.deviceTypeKey)
      )),
      tags = if (device.tags.isEmpty) {
        DeviceTypeUtil.defaultTags(device.deviceTypeKey)
      } else {
        device.tags
      }
    )

  }

}
