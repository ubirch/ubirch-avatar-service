package com.ubirch.avatar.core.prometheus

import com.ubirch.avatar.config.Config
import io.prometheus.client.Histogram

class Timer(timerName: String) {

  private val prefix = Config.enviroment.replace("-", "_").trim

  private val enabled = Config.prometheusEnabled

  private var timer: Histogram.Timer = null

  def start: Unit = {
    if (enabled && timer == null) {
      timer = requestLatency.startTimer()
    }
  }

  def stop: Unit = {
    if (timer != null) {
      timer.observeDuration()
      timer = null
    }
  }

  private val requestLatency = Histogram
    .build()
    .name(s"${prefix}_${timerName}_latency_seconds")
    .help(s"$timerName latency in seconds.")
    .register()

}
