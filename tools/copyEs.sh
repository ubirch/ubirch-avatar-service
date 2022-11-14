#!/bin/bash

# https://github.com/taskrabbit/elasticsearch-dump

if [ ! -z $1 ] && [ ! -z $2 ]; then

    INDICES="ubirch-devices ubirch-device-history ubirch-device-raw-data ubirch-device-raw-data-anchored ubirch-avatar-state"

    SOURCE=$1:9200
    DESTINATION=$2:9200

    for index in $INDICES; do
      docker run --rm -ti taskrabbit/elasticsearch-dump \
        --input=http://${SOURCE}/$index \
        --output=http://${DESTINATION}/$index \
        --type=data
    done

fi