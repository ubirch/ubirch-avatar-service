package com.ubirch.services.util

import com.ubirch.avatar.core.device.DeviceDataRawManager
import com.ubirch.avatar.model.rest.device.DeviceDataRaw
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

object ReprocessPayloads {

  def reprocessByDay(day: DateTime)(implicit ec: ExecutionContext): Future[Seq[DeviceDataRaw]] = {
    DeviceDataRawManager.history(day, 0, 1000).map { d =>
      d
    }
  }

}
