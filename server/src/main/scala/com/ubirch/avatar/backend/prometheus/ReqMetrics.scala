package com.ubirch.avatar.backend.prometheus

import com.ubirch.avatar.config.Config
import io.prometheus.client.{Counter, Histogram}

class ReqMetrics(metricName: String) {

  private val prefix = Config.enviroment.replace("-", "_").trim

  private val enabled = Config.prometheusEnabled

  private var timer: Histogram.Timer = null

  def inc(): Unit = if (enabled) requests.inc()

  private val requests: Counter = Counter.build()
    .name(s"${prefix}_requests_${metricName}_total")
    .help(s"Total requests: $metricName")
    //.labelNames("device_update_total")
    .register()

  def incError(): Unit = if (enabled) requestsErrors.inc()

  private val requestsErrors: Counter = Counter.build()
    .name(s"${prefix}_requests_${metricName}_failed_total")
    .help(s"Total failed requests: $metricName")
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
    .name(s"${prefix}_${metricName}_latency_seconds")
    .help(s"$metricName latency in seconds.")
    .register()
}
