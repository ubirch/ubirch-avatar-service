package com.ubirch.avatar.model

import java.util.UUID

import com.ubirch.avatar.config.Const
import com.ubirch.avatar.model.db.device.Device
import com.ubirch.util.crypto.hash.HashUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime
import org.json4s.JValue
import org.json4s.native.JsonMethods._

/**
  * author: cvandrei
  * since: 2016-09-23
  */
object DummyDevices {

  def minimalDevice(deviceId: String = UUIDUtil.uuidStr,
                    owners: Set[UUID] = Set.empty,
                    groups: Set[UUID] = Set.empty,
                    hwDeviceId: String = UUIDUtil.uuidStr
                   ): Device = {

    Device(
      deviceId = deviceId,
      owners = owners,
      groups = groups,
      hwDeviceId = hwDeviceId,
      hashedHwDeviceId = HashUtil.sha256HexString(hwDeviceId)
    )

  }

  def device(deviceId: String = UUIDUtil.uuidStr,
             owners: Set[UUID] = Set(UUIDUtil.uuid),
             groups: Set[UUID] = Set(UUIDUtil.uuid),
             deviceName: String = "testHans001",
             hwDeviceId: String = UUIDUtil.uuidStr,
             deviceTypeKey: String = Const.ENVIRONMENTSENSOR,
             deviceProperties: Option[JValue] = None
            ): Device = {

    Device(
      deviceId = deviceId,
      owners = owners,
      groups = groups,
      deviceName = deviceName,
      hwDeviceId = hwDeviceId.toLowerCase,
      deviceTypeKey = deviceTypeKey,
      hashedHwDeviceId = HashUtil.sha512Base64(hwDeviceId),
      deviceProperties = deviceProperties
    )

  }

  lazy val all: Seq[Device] = Seq(device1, device2, device3, device4)

  lazy val deviceMap: Map[String, Device] = Map(
    device1Id -> device1,
    device2Id -> device2,
    device3Id -> device3,
    device4Id -> device4
  )

  lazy val device1Id = "0c5a19bf-194c-40ea-bf46-0416a176aedb"
  lazy val device2Id = "0c5a19bf-194c-40ea-bf46-0416a176aedc"
  lazy val device3Id = "0c5a19bf-194c-40ea-bf46-0416a176aedd"
  lazy val device4Id = "0c5a19bf-194c-40ea-bf46-0416a176aede"

  lazy val device1 = Device(
    deviceId = device1Id,
    groups = Set(UUIDUtil.uuid),
    deviceTypeKey = "lightsSensor",
    deviceName = "lightsSensor_LU_8caa2520-d8f0-4c85-9705-4707054f4e11",
    hwDeviceId = "860719022152999",
    tags = Set("ubirch#0", "sensor"),
    deviceConfig = Some(device1Config),
    deviceProperties = Some(device1Properties),
    avatarLastUpdated = Some(DateTime.now.minusMinutes(1)),
    created = DateTime.now.minusDays(60),
    updated = Some(DateTime.now.minusDays(2)),
    deviceLastUpdated = Some(DateTime.now.minusMinutes(5))
  )

  lazy val device2 = Device(
    deviceId = device2Id,
    groups = Set(UUIDUtil.uuid),
    deviceTypeKey = "temperaturesSensor",
    deviceName = "temperaturesSensor_TU_8caa2520-d8f0-4c85-9705-4707054f4e11",
    hwDeviceId = "860719022152999",
    tags = Set("ubirch#2"),
    deviceConfig = Some(device2Config),
    deviceProperties = Some(device2Properties),
    avatarLastUpdated = Some(DateTime.now.minusMinutes(1)),
    created = DateTime.now.minusDays(60),
    updated = Some(DateTime.now.minusDays(2)),
    deviceLastUpdated = Some(DateTime.now.minusMinutes(5))
  )

  lazy val device3 = Device(
    deviceId = device3Id,
    groups = Set(UUIDUtil.uuid),
    deviceTypeKey = "machineSensor",
    deviceName = "machineSensor_WM_8caa2520-d8f0-4c85-9705-4707054f4e11",
    hwDeviceId = "860719022152999",
    tags = Set("ubirch#0", "sensor"),
    deviceConfig = Some(device3Config),
    deviceProperties = Some(device3Properties),
    avatarLastUpdated = Some(DateTime.now.minusMinutes(1)),
    created = DateTime.now.minusDays(60),
    updated = Some(DateTime.now.minusDays(2)),
    deviceLastUpdated = Some(DateTime.now.minusMinutes(5))
  )

  lazy val device4 = Device(
    deviceId = device4Id,
    groups = Set(UUIDUtil.uuid),
    deviceTypeKey = "trackleSensor",
    deviceName = "trackleSensor_LU_8caa2520-d8f0-4c85-9705-4707054f4e11",
    hwDeviceId = "860719022152999",
    tags = Set("ubirch#0", "sensor"),
    deviceConfig = Some(device4Config),
    deviceProperties = Some(device4Properties),
    subQueues = None,
    avatarLastUpdated = Some(DateTime.now.minusMinutes(1)),
    created = DateTime.now.minusDays(60),
    updated = Some(DateTime.now.minusDays(2)),
    deviceLastUpdated = Some(DateTime.now.minusMinutes(5))
  )

  lazy val device1Config: JValue = parse(
    """{
      |  "i": 3600,
      |  "ir": 191,
      |  "s": 0
      |}""".stripMargin)

  lazy val device1Properties: JValue = parse(
    """{
      |  "countryCode": "LU"
      |}""".stripMargin)

  lazy val avatar1Desired: JValue = parse(
    """{
      |  "i": 1800,
      |  "bf": 1,
      |  "r": 13944,
      |  "g": 21696,
      |  "b": 17840
      |}""".stripMargin)

  lazy val avatar1Reported: JValue = parse(
    """{
      |  "la": "52.502769",
      |  "lo": "13.477947",
      |  "ba": 13,
      |  "lp": 55,
      |  "e": 0,
      |  "i": 1800,
      |  "bf": 1,
      |  "r": 13944,
      |  "g": 21696,
      |  "b": 17840
      |}""".stripMargin)

  lazy val device2Config: JValue = parse(
    """{
      |  "i": 3600,
      |  "ir": 191,
      |  "s": 0
      |}""".stripMargin)

  lazy val device2Properties: JValue = parse(
    """{
      |  "countryCode": "LU"
      |}""".stripMargin)

  lazy val avatar2Desired: JValue = parse(
    """{
      |  "i": 1800,
      |  "bf": 1,
      |  "r": 13944,
      |  "g": 21696,
      |  "b": 17840
      |}""".stripMargin)

  lazy val avatar2Reported: JValue = parse(
    """{
      |  "la": "52.502769",
      |  "lo": "13.477947",
      |  "ba": 13,
      |  "lp": 55,
      |  "e": 0,
      |  "i": 1800,
      |  "bf": 1,
      |  "r": 13944,
      |  "g": 21696,
      |  "b": 17840
      |}""".stripMargin)

  lazy val device3Config: JValue = parse(
    """{
      |  "i": 3600,
      |  "ir": 191,
      |  "s": 0
      |}""".stripMargin)

  lazy val device3Properties: JValue = parse(
    """{
      |  "countryCode": "LU"
      |}""".stripMargin)

  lazy val avatar3Desired: JValue = parse(
    """{
      |  "la": "52.502769",
      |  "lo": "13.477947",
      |  "ba": 13,
      |  "lp": 55,
      |  "e": 0,
      |  "i": 1800,
      |  "bf": 1,
      |  "r": 13944,
      |  "g": 21696,
      |  "b": 17840
      |}""".stripMargin)

  lazy val avatar3Reported: JValue = parse(
    """{
      |  "la": "52.502769",
      |  "lo": "13.477947",
      |  "ba": 13,
      |  "lp": 55,
      |  "e": 0,
      |  "i": 1800,
      |  "bf": 1,
      |  "r": 13944,
      |  "g": 21696,
      |  "b": 17840
      |}""".stripMargin)

  lazy val device4Config: JValue = parse(
    """{
      |  "i": 3600,
      |  "ir": 191,
      |  "s": 0
      |}""".stripMargin)

  lazy val device4Properties: JValue = parse(
    """{
      |  "countryCode": "LU"
      |}""".stripMargin)

  lazy val avatar4Desired: JValue = parse(
    """{
      |  "i": 1800,
      |  "bf": 1,
      |  "r": 13944,
      |  "g": 21696,
      |  "b": 17840
      |}""".stripMargin)

  lazy val avatar4Reported: JValue = parse(
    """{
      |  "la": "52.502769",
      |  "lo": "13.477947",
      |  "ba": 13,
      |  "lp": 55,
      |  "e": 0,
      |  "i": 1800,
      |  "bf": 1,
      |  "r": 13944,
      |  "g": 21696,
      |  "b": 17840
      |}""".stripMargin)

}
