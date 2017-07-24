#!/usr/bin/env bash
cd ..

echo "======"
echo "====== STEP 1/1: start to create a device with some data (avatar-service=$AVATAR_CMD_IMPORT_AVATAR_BASE_URL)"
echo "======"
./sbt "cmdtools/runMain com.ubirch.avatar.cmd.ImportTrackleRemote"
echo "======"
echo "====== STEP 1/1: finished creation of device with some data (avatar-service=$AVATAR_CMD_IMPORT_AVATAR_BASE_URL)"
echo "======"
