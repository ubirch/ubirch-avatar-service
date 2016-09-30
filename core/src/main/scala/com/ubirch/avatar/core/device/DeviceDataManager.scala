package com.ubirch.avatar.core.device

import com.ubirch.avatar.config.Config
import com.ubirch.avatar.model.DeviceData

/**
  * author: cvandrei
  * since: 2016-09-30
  */
object DeviceDataManager {

  def history(deviceId: String, from: Long = 0, size: Long = Config.esDefaultSize): Seq[DeviceData] = Seq.empty // TODO implement

}
