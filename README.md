# ubirch-avatar-service

ubirch device-configuration and -dataflow service

## General Information

TODO

## Scala Dependencies

### `config`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.avatar" %% "config" % "0.2.0-SNAPSHOT"
    )

### `core`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.avatar" %% "core" % "0.2.0-SNAPSHOT"
    )

### `model-rest`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.avatar" %% "model-rest" % "0.2.0-SNAPSHOT"
    )

### `model-db`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.avatar" %% "model-db" % "0.2.0-SNAPSHOT"
    )
        
### `server`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.bintrayRepo("hseeberger", "maven")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.avatar" %% "server" % "0.2.0-SNAPSHOT"
    )
        
### `server`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.avatar" %% "model" % "0.2.0-SNAPSHOT"
    )

## REST Methods

### Welcome / Health

    curl localhost:8080/

If server is healthy response is:

    200 {"version":"1.0","status":"OK","message":"Welcome to the ubirchAvatarService"}

### Device Information

#### TODO: title

TODO: description

    curl -XGET localhost:8080/api/avatarService/v1/device

    curl -XPOST localhost:8080/api/avatarService/v1/device

#### TODO: title

TODO: description

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>

    curl -XPOST localhost:8080/api/avatarService/v1/device/<DEVICE_ID>

    curl -XDELETE localhost:8080/api/avatarService/v1/device/<DEVICE_ID>

#### Device State

TODO: description

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/state

    curl -XPOST localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/state

#### TODO: title

TODO: description

    curl -XGET localhost:8080/api/avatarService/v1/device/stub/<DEVICE_ID>

### Device Data

#### Raw

Raw data comes directly from devices and is not yet human readable.

    curl -XPOST localhost:8080/api/avatarService/v1/device/data/raw -d '{
      "deviceId": "57a7892e-e707-4256-81e4-2e579213e6b8",
      "messageId": "8aa3d0ec-9ec8-4785-93e9-6fd1705dace6",
      "deviceType": "lightsLamp",
      "timestamp": "2016-06-30T11:39:51Z",
      "deviceTags": [
        "ubirch#0",
        "actor"
      ],
      "deviceMessage": {
        "foo": 23,
        "bar": "ubirch-sensor-data"
      }
    }'

#### History

Historic data is generated by sending in raw data which is then transformed to "processed" data.

The main difference between raw and processed data is simple. Raw data has been generated by devices and is not human 
readable. Applying a device specific transformation to raw data we get processed data which is human readable.

Query historic device data (CAUTION: `from` and `page_size` may be zero or larger).

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/data/history

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/data/history/<FROM>

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/data/history/<FROM>/<PAGE_SIZE>

## Configuration

TODO


## AWS

### AWS CLI

On MacOS you can install the aws-cli tool through brew:

    brew install awscli

To configure it then run:

    aws configure

The default region should be `us-east-1` while the output format can remain None since it's not relevant yet.

### AWS Configuration

The AvatarService opens a connection to AWS which depends on the following environment variables:

    export AWS_ACCESS_KEY_ID=foo
    export AWS_SECRET_ACCESS_KEY=bar


## Deployment Notes

### Elasticsearch

The service requires the following mappings for things to work as expected:

    curl -XPOST 'localhost:9200/ubirch-device-data' -H "Content-Type: application/json" -d '{
      "mappings": {
        "message" : {
          "properties" : {
            "deviceId" : {
              "type" : "string",
              "index": "not_analyzed"
            },
            "messageId" : {
              "type" : "string",
              "index": "not_analyzed"
            }
          }
        }
      }
    }'

## Automated Tests

TODO

## Local Setup

TODO

## create docker image

    ./sbt server/docker
