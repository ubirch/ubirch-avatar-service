#!/bin/sh

export ES_URL="localhost:9200"

echo "===ubirch-devices"
echo "drop index: "
curl -XDELETE http://localhost:9200/ubirch-devices
echo "\ncreate index: "
curl -XPOST "$ES_URL/ubirch-devices" -H "Content-Type: application/json" -d '{
  "mappings": {
    "device" : {
      "properties" : {
        "deviceId" : {
          "type" : "string",
          "index": "not_analyzed"
        },
        "hwDeviceId" : {
          "type" : "string",
          "index": "not_analyzed"
        },
        "hashedHwDeviceId" : {
          "type" : "string",
          "index": "not_analyzed"
        }
      }
    }
  }
}'

echo "\n===ubirch-device-raw-data"
echo "drop index: "
curl -XDELETE http://localhost:9200/ubirch-device-raw-data
echo "\ncreate index: "
curl -XPOST "$ES_URL/ubirch-device-raw-data" -H "Content-Type: application/json" -d '{
  "mappings": {
    "devicemessage" : {
      "properties" : {
        "timestamp": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
        },
        "a" : {
          "type" : "string",
          "index": "not_analyzed"
        },
        "id" : {
          "type" : "string",
          "index": "not_analyzed"
        }
      }
    }
  }
}'

echo "\n===ubirch-device-raw-data-anchored"
echo "drop index: "
curl -XDELETE http://localhost:9200/ubirch-device-raw-data-anchored
echo "\ncreate index: "
curl -XPOST "$ES_URL/ubirch-device-raw-data" -H "Content-Type: application/json" -d '{
  "mappings": {
    "devicemessage" : {
      "properties" : {
        "timestamp": {
            "type": "date",
            "format": "strict_date_optional_time||epoch_millis"
        },
        "a" : {
          "type" : "string",
          "index": "not_analyzed"
        },
        "id" : {
          "type" : "string",
          "index": "not_analyzed"
        }
      }
    }
  }
}'

echo "\n===ubirch-device-history"
echo "drop index: "
curl -XDELETE http://localhost:9200/ubirch-device-history
echo "\ncreate index: "
curl -XPOST "$ES_URL/ubirch-device-history" -H "Content-Type: application/json" -d '{
  "mappings": {
    "devicedata" : {
      "properties" : {
        "timestamp": {
          "type": "date",
          "format": "strict_date_optional_time||epoch_millis"
        },
        "deviceId" : {
          "type" : "string",
          "index": "not_analyzed"
        },
        "messageId" : {
          "type" : "string",
          "index": "not_analyzed"
        },
        "deviceDataRawId" : {
          "type" : "string",
          "index": "not_analyzed"
        },
        "id" : {
          "type" : "string",
          "index": "not_analyzed"
        },
        "a" : {
          "type" : "string",
          "index": "not_analyzed"
        }
      }
    }
  }
}'

echo "\n===ubirch-device-type"
echo "drop index: "
curl -XDELETE http://localhost:9200/ubirch-device-type
echo "\ncreate index: "
curl -XPOST "$ES_URL/ubirch-device-type" -H "Content-Type: application/json" -d '{
  "mappings": {
    "devicetype" : {
      "properties" : {
        "key" : {
          "type" : "string",
          "index": "not_analyzed"
        }
      }
    }
  }
}'
