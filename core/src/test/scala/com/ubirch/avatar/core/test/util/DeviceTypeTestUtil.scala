package com.ubirch.avatar.core.test.util

import com.ubirch.avatar.core.device.DeviceTypeManager
import com.ubirch.avatar.model.device.DeviceType
import com.ubirch.avatar.util.model.DeviceTypeUtil

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2016-11-11
  */
object DeviceTypeTestUtil {

  def storeSeries(prefix: String = DeviceTypeUtil.defaultKey,
                  elementCount: Int = 5,
                  startIndex: Int = 0,
                  sleep: Int = 1000
                 ): Seq[DeviceType] = {

    val dataSeries = DeviceTypeUtil.dataSeries(
      prefix = prefix,
      elementCount = elementCount,
      startIndex = startIndex
    )

    dataSeries foreach { dt =>
      Await.result(DeviceTypeManager.create(dt), 1 second)
    }
    Thread.sleep(sleep)

    dataSeries

  }

}
