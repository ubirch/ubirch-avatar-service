#!/usr/bin/env bash

echo "starting avatar service"

java="java"

jar="./server/target/scala-2.11/server-assembly-0.3.4-SNAPSHOT.jar"

params="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Dconfig.resource=application.conf -Dlogback.configurationFile=logback.test.xml"

jvmparams="-Xms1g -Xmx2g -Djava.awt.headless=true -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -XX:+DisableExplicitGC -Dfile.encoding=UTF-8"

$java $params -jar $jar $jvmparams