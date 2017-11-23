package com.ubirch.avatar.core.prometheus

import com.ubirch.avatar.config.Config
import io.prometheus.client.{Counter, Histogram}

class Timer(timerName: String) {

  private val enabled = Config.prometheusEnabled

  private var timer : Histogram.Timer =  null

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
    .name(s"${timerName}_latency_seconds")
    .help(s"$timerName latency in seconds.")
    .register()

}