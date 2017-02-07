#!/usr/bin/env bash

echo "starting avatar service"

java="java"

jar="./server/target/scala-2.11/server-assembly-0.3.5-SNAPSHOT.jar"

params="-Dconfig.resource=application.dev.conf -Dlogback.configurationFile=logback.dev.xml"

jvmparams="-Xms1g -Xmx2g -Djava.awt.headless=true -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -XX:+DisableExplicitGC -Dfile.encoding=UTF-8"

$java $params -jar $jar $jvmparams