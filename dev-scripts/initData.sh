#!/usr/bin/env bash
cd ..

echo "======"
echo "====== STEP 1/1: start to create a device with some data (this only works with a running avatar-service!!!)"
echo "======"
./sbt "cmdtools/runMain com.ubirch.avatar.cmd.InitData"
echo "======"
echo "====== STEP 1/1: finished creation of device with some data (this only works with a running avatar-service!!!)"
echo "======"
