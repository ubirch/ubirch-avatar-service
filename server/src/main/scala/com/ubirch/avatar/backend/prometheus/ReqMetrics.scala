package com.ubirch.avatar.backend.prometheus

import io.prometheus.client.{Counter, Histogram}

class ReqMetrics(counterName:String) {

  val requests: Counter = Counter.build()
    .name(s"requests_${counterName}_total")
    .help(s"Total requests: $counterName")
    //.labelNames("device_update_total")
    .register()

  val requestsErrors: Counter = Counter.build()
    .name(s"requests_${counterName}_failed_total")
    .help(s"Total failed requests: $counterName")
    //.labelNames("device_update_failed_total")
    .register()

  val requestLatency = Histogram
    .build()
    .name(s"${counterName}_latency_seconds")
    .help(s"$counterName latency in seconds.")
    .register()

}