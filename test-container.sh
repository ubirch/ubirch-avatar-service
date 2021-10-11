#!/bin/bash -x

ES_HOST=192.168.178.23
ES_PORT_TCP=9300
ES_PORT_HTTP=9200

export ES_HOST ES_PORT_HTTP ES_PORT_TCP
# docker run --rm -p 9200:9200 -p 9300:9300 elasticsearch
# docker run --rm -e ELASTICSEARCH_URL=http://$ES_HOST:$ES_PORT_HTTP -p 5601:5601 kibana
# start
docker run --rm \
  -e ES_HOST \
  -e ES_PORT_HTTP \
  -e ES_PORT_TCP \
  -p 8080:8080 \
  ubirch-avatar-service
