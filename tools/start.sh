#!/usr/bin/env bash

echo "starting avatar service"


java -Dlogback.appender=STDOUT -Dlogback.configurationFile=/opt/ubirch/etc/logback.xml -Dconfig.file=/opt/ubirch/etc/application.conf -jar /opt/jar/server-assembly-0.3.5-SNAPSHOT.jar -Dfile.encoding=UTF-8 -XX:+UseCMSInitiatingOccupancyOnly -XX:+DisableExplicitGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -Xms1g -Xmx2g -Djava.awt.headless=true
