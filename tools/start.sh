#!/usr/bin/env bash
# Copyright 2017 ubirch GmbH. All Rights Reserved.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#     http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# Script to run the ubirch Avatar service inside the container



echo "starting avatar service"

JVM_PARAMS="-Dfile.encoding=UTF-8 -XX:+UseCMSInitiatingOccupancyOnly -XX:+DisableExplicitGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -Xms1g -Xmx2g -Djava.awt.headless=true"

if [ -z $DEBUG  ]; then
  APP_PARAMS="-Dlogback.configurationFile=/opt/ubirch/etc/logback.xml -Dconfig.file=/opt/ubirch/etc/application.conf"
else
  APP_PARAMS="-Dlogback.configurationFile=logback.test.xml -Dconfig.file=/opt/ubirch/etc/application.conf"
fi

java  $JVM_PARAMS $APP_PARAMS -jar /opt/jar/@@build-artefact@@
