package com.ubirch.avatar.cmd

/**
  * Created by derMicha on 30/03/17.
  */
object TestEsXpack extends App {

  import java.net.InetAddress

  import org.elasticsearch.client.transport.TransportClient
  import org.elasticsearch.common.settings.Settings
  import org.elasticsearch.common.transport.InetSocketTransportAddress
  // Build the settings for our client.// Build the settings for our client.

  val clusterId = "3332527641ff8e2c90e3837194cf4b1c" // Your cluster ID here

  val region = "us-east-1" // Your region here

  val enableSsl = true

  val settings = Settings.settingsBuilder.put("transport.ping_schedule", "5s")
    .put("cluster.name", clusterId)
    .put("action.bulk.compress", false)
    .put("shield.transport.ssl", enableSsl)
    .put("request.headers.X-Found-Cluster", clusterId)
    .put("shield.user", "readwrite:$2a$12$KODiWXdY3cdpCuxfOly6vOGk20Ll5gr69WVXztq7rzYd8Y5FLwg7O").build

  val hostname = clusterId + "." + region + ".aws.found.io"
  // Instantiate a TransportClient and add the cluster to the list of addresses to connect to.
  // Only port 9343 (SSL-encrypted) is currently supported.
  val client = TransportClient
    .builder
    .addPlugin(classOf[ShieldPlugin])
    .settings(settings)
    .build
    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostname), 9343))


  val nodes = client.connectedNodes()
  println(nodes.size())
}
