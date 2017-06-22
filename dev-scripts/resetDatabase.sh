#!/usr/bin/env bash
cd ..

echo "======"
echo "====== STEP 1/1: start to delete database"
echo "======"
./sbt "cmdtools/runMain com.ubirch.avatar.cmd.ClearDb"
echo "======"
echo "====== STEP 1/1: finished deleting database"
echo "======"
