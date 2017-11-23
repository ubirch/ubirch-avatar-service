package com.ubirch.avatar.backend.prometheus

import com.ubirch.avatar.config.Config
import io.prometheus.client.{Counter, Histogram}

class ReqMetrics(counterName: String) {

  private val enabled = Config.prometheusEnabled

  private var timer : Histogram.Timer =  null

  def inc(): Unit = if (enabled) requests.inc()

  private val requests: Counter = Counter.build()
    .name(s"requests_${counterName}_total")
    .help(s"Total requests: $counterName")
    //.labelNames("device_update_total")
    .register()

  def incError(): Unit = if (enabled) requestsErrors.inc()

  private val requestsErrors: Counter = Counter.build()
    .name(s"requests_${counterName}_failed_total")
    .help(s"Total failed requests: $counterName")
    //.labelNames("device_update_failed_total")
    .register()


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
    .name(s"${counterName}_latency_seconds")
    .help(s"$counterName latency in seconds.")
    .register()

}