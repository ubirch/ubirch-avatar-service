#!/usr/bin/env bash
cd ..

function runInit() {

    token=$1
    baseUrl=$2

    echo "======"
    echo "====== STEP 1/1: start to create a device with some data (avatar-service=$baseUrl)"
    echo "======"

    export AVATAR_USER_TOKEN=$token
    ./sbt "cmdtools/runMain com.ubirch.avatar.cmd.ImportTrackle"

    echo "======"
    echo "====== STEP 1/1: finished creation of device with some data (avatar-service=$baseUrl)"
    echo "======"

}

if [ $# -eq 1 ]
  then
    runInit $1 $AVATAR_BASE_URL
else
    echo "wrong number of arguments. please run as follows:"
    echo "    ./importTrackle.sh \$AVATAR_USER_TOKEN"
fi
