# ubirch-avatar-service

ubirch device-configuration and -dataflow service

## General Information

ubirch Avatar Service is responsible for:

* offering ubirch IoT devices an endpoint to sync their state
* processing incoming raw data from ubirch IoT devices
** validating signatures
** transforming raw data
* offering CRUD API to manage ubirch IoT Devices
* managing device states using Amazon AWS IoT
* publishing processed data for further processing (AWS SQS)

## Release History

### Version 0.3.11 (tbd)

* update to _json4s_ version 3.5.1
* update to _de.heikoseeberger:akka-http-json4s_ version 1.14.0
* update Akka HTTP to version 10.0.5
* update _com.ubirch.util:json_ to version 0.3.4
* update _com.ubirch.util:json-auto-convert_ to version 0.3.4
* update _com.ubirch.notary:notary-client_ to version 0.3.2
* update _com.ubirch.util:elasticsearch-util_ to version 2.0.0
* update _com.ubirch.util:elasticsearch-client-binary_ to version 2.0.1
* update to Elasticsearch 5.3
* update _com.ubirch.util:oidc-utils_ to version 0.4.0
* update mappings to Elasticsearch 5.3
* rename module _model_ to _model-rest_ and introduce module _model-db_

### Version 0.3.10 (2017-03-01)

* add field `txHashLinkHtml` to `DeviceDataRaw`

### Version 0.3.9 (2017-02-28)

* update dependency `com.ubirch.notary:client:0.3.0` to 0.3.1

### Version 0.3.8 (2017-02-28)

* update dependency `com.ubirch.notary:client:0.2.6` to 0.3.0
* remember `txHashLink` when writing data to index `ubirch-device-raw-data-anchored`

### Version 0.3.7 (2017-02-28)

* upgrade lib: `Akka HTTP` from version 2.4.11 (experimental) to 10.0.3 (stable)
* upgrade lib: `Akka` from version 2.4.11 to 2.4.17
* upgrade rest-akka-http* modules to version 0.3.3 (add `Authorization` to CORS allow-headers)
* new routes
  * /device/$DEVICE_ID/data/history/bydate/from/$FROM_DATE/to/$TO_DATE
  * /device/$DEVICE_ID/data/history/bydate/before/$DATE
  * /device/$DEVICE_ID/data/history/bydate/after/$DATE
  * /device/$DEVICE_ID/data/history/day/$DATE
* save bitcoin tx hashes in a new index: "ubirch-device-raw-data-anchored" (only relevant for raw data with NotaryService being used)
* update `scalatest` from 3.0.0 to 3.0.1
* update to `elasticsearch-client-binary` version 0.5.2
* bulk write of `DeviceDataRaw` and `DeviceDataProcessed`
* add AvatarStateManager

### Version 0.3.6 (2017-02-07)

* update dependency "com.ubirch.notary:client:0.2.5" to 0.2.6 (getting rid of com.ubirch.util:json-auto-convert:0.1)

### Version 0.3.5 (2017-02-07)

* minor refactorings
* delete route: POST /device/<DEVICE_ID>
* fix: read AWS from config instead of System.getenv()
* fixed `ClearDb`
* updated some of the dependencies
* added PTX temperature conversions

### Version 0.3.4 (2016-12-13)

* added first version of raw data -> history data transformation
* extended DeviceType, which is contains now displayKeys: Array[String]

## Scala Dependencies

### `aws`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "aws" % "0.3.11-SNAPSHOT"
)
```

### `client`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("rick-beton", "maven") // BeeClient
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "client" % "0.3.11-SNAPSHOT"
)
```

### `cmdtools`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "cmdtools" % "0.3.11-SNAPSHOT"
)
```

### `config`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "config" % "0.3.11-SNAPSHOT"
)
```

### `core`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "core" % "0.3.11-SNAPSHOT"
)
```

### `model-db`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "model-db" % "0.3.11-SNAPSHOT"
)
```

### `model-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "model-rest" % "0.3.11-SNAPSHOT"
)
```

### `server`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("hseeberger", "maven"),
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "server" % "0.3.11-SNAPSHOT"
)
```

### `test-base`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("hseeberger", "maven"),
  Resolver.bintrayRepo("rick-beton", "maven") // BeeClient
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "test-base" % "0.3.11-SNAPSHOT"
)
```

### `util`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "util" % "0.3.11-SNAPSHOT"
)
```


## REST Methods

### Welcome / Health

    curl localhost:8080/

If server is healthy response is:

    200 {"version":"1.0","status":"OK","message":"Welcome to the ubirchAvatarService"}

### Device CRUD

#### LIST all devices

returns an array of all devices the authenticated user has connected

    curl -XGET localhost:8080/api/avatarService/v1/device

to list devices as short info objects use stub endpoint

    curl -XGET localhost:8080/api/avatarService/v1/device/stub


#### CREATE device

creates a new device

    curl -XPOST localhost:8080/api/avatarService/v1/device -H "Content-Type: application/json" -d '{
        "deviceId":"5df0c9b7-564a-4b90-8f1b-998fbe1a1cbf",
        "hwDeviceId":"hdkljhdklghdfkjlghsdfkljghdfskl",
        "deviceName":"new device",
        "deviceTypeKey":"lightsLamp",
        "deviceProperties":{},
        "deviceConfig":{
            "i":900,
            "bf":0
        },
        "tags":[
            "ubirch#0",
            "actor","btcDemo"
        ]}'


#### READ, EDIT, DELETE device with ID

READ device with given id

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>

UPDATE device with given id

    curl -XPUT localhost:8080/api/avatarService/v1/device/<DEVICE_ID>  -H "Content-Type: application/json" -d '{
          "deviceId":"5df0c9b7-564a-4b90-8f1b-998fbe1a1cbf",
          "hwDeviceId":"hdkljhdklghdfkjlghsdfkljghdfskl",
          "deviceName":"new device",
          "deviceTypeKey":"lightsLamp",
          "deviceProperties":{},
          "deviceConfig":{
              "i":900,
              "bf":0
          },
          "tags":[
              "ubirch#0",
              "actor","btcDemo"
          ]}'

DELETE device with given id

    curl -XDELETE localhost:8080/api/avatarService/v1/device/<DEVICE_ID>

#### Device State

get state of device with given id

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/state

### Device Data

#### Raw

Raw data comes directly from devices and is not yet human readable.

    curl -XPOST localhost:8080/api/avatarService/v1/device/data/raw -H "Content-Type: application/json" -d '{
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

#### History (From-To)

Historic data is generated by sending in raw data which is then transformed to "processed" data.

The main difference between raw and processed data is simple. Raw data has been generated by devices and is not human
readable. Applying a device specific transformation to raw data we get processed data which is human readable.

Query historic device data (CAUTION: `from` and `page_size` may be zero or larger):

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/data/history

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/data/history/<FROM>

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/data/history/<FROM>/<PAGE_SIZE>

#### History (By Date)

Historic data is generated by sending in raw data which is then transformed to "processed" data.

The main difference between raw and processed data is simple. Raw data has been generated by devices and is not human 
readable. Applying a device specific transformation to raw data we get processed data which is human readable.

Query historic device data **between** a `from` and `to` (including both borders):

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/data/history/byDate/from/$FROM_DATE/to/$TO_DATE

Query historic device data *before* a specific date:

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/data/history/byDate/before/$DATE

Query historic device data *after* a specific date:

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/data/history/byDate/after/$DATE

Query historic device data of a *whole* day:

    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/data/history/byDate/day/$DATE


### Device Types

Devices have types and this set of methods allows us to manage them.

#### Query all available device types

    curl -XGET localhost:8080/api/avatarService/v1/device/deviceType

#### Create device type

    curl -XPOST localhost:8080/api/avatarService/v1/device/deviceType -H "Content-Type: application/json" -d '{
        "key": "trackle",
        "name": {
          "de": "Trackle",
          "en": "Trackle"
        },
        "icon": "trackle",
        "defaults": {
          "properties": {},
          "config": {
            "i": 60
          },
          "tags": [
            "ubirch#1",
            "actor",
            "trackle"
          ]
        }
      }'

##### Response (Success)

    HTTP/1.1 200 OK
    Access-Control-Allow-Origin: *
    Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
    Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent
    Access-Control-Allow-Credentials: true
    Server: ubirch-avatar-service
    Date: Thu, 10 Nov 2016 16:30:51 GMT
    Content-Type: application/json
    Content-Length: 158

    {"key":"trackle","name":{"de":"Trackle","en":"Trackle"},"icon":"trackle","defaults":{"properties":{},"config":{"i":60},"tags":["ubirch#1","actor","trackle"]}}

##### Response (Error)

    HTTP/1.1 400 Bad Request
    Access-Control-Allow-Origin: *
    Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
    Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent
    Access-Control-Allow-Credentials: true
    Server: ubirch-avatar-service
    Date: Thu, 10 Nov 2016 16:35:40 GMT
    Content-Type: application/json
    Content-Length: 199

    {
      "version" : "1.0",
      "status" : "NOK",
      "errorType" : "CreateError",
      "errorMessage": "another deviceType with key=trackle already exists or otherwise something else on the server went wrong"
    }

#### Update Device Type

    curl -XPUT localhost:8080/api/avatarService/v1/device/deviceType -H "Content-Type: application/json" -d '{
        "key": "trackle",
        "name": {
          "de": "Trackle",
          "en": "Trackle"
        },
        "icon": "trackle",
        "defaults": {
          "properties": {},
          "config": {
            "i": 120
          },
          "tags": [
            "ubirch#0",
            "actor",
            "trackle"
          ]
        }
      }'

##### Response (Success)

    HTTP/1.1 200 OK
    Access-Control-Allow-Origin: *
    Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
    Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent
    Access-Control-Allow-Credentials: true
    Server: ubirch-avatar-service
    Date: Thu, 10 Nov 2016 16:33:24 GMT
    Content-Type: application/json
    Content-Length: 159

    {"key":"trackle","name":{"de":"Trackle","en":"Trackle"},"icon":"trackle","defaults":{"properties":{},"config":{"i":120},"tags":["ubirch#0","actor","trackle"]}}

##### Response (Error)

    HTTP/1.1 400 Bad Request
    Access-Control-Allow-Origin: *
    Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
    Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent
    Access-Control-Allow-Credentials: true
    Server: ubirch-avatar-service
    Date: Thu, 10 Nov 2016 16:27:31 GMT
    Content-Type: application/json
    Content-Length: 186

    {
      "version" : "1.0",
      "status" : "NOK",
      "errorType" : "UpdateError",
      "errorMessage": "no deviceType with key=trackle exists or otherwise something else on the server went wrong"
    }

#### Create default device types but only if no other types exist in the database:

    curl -XGET localhost:8080/api/avatarService/v1/device/deviceType/init


## Configuration

The ubirch avatar service has several external dependencies:
 * AWS SQS
 * ElasticSearch

 Those are configured via environment variables.
 
### AWS SQS
 In order to talk to AWS SQS the service needs two SQS targets:

    SQS_UBIRCH_TRANSFORMER_INBOX=<string>
    SQS_UBIRCH_TRANSFORMER_OUTBOX=<string>

 Those queues need authentication with AWS credentials. These are passed via

    AWS_ACCESS_KEY_ID=<string>
    AWS_SECRET_ACCESS_KEY=<string>

### ElasticSearch
Avatar service is using ElasticSearch for logging and device management. It needs a specific ES version and access to Port 9200 (HTTP) and Port 93000 (TCP)

		ES_HOST=elasticsearch
		ES_PORT_HTTP=9200
		ES_PORT_TCP=9300
		
### Debug Output
Since ubirch avatar service is using ElastiSearch for logging it can be hard to figure out what's not working if the connection to ES can't be established. If you set 

		DEBUG=true

as environment variable then the logging is send to STDOUT.

## AWS

### AWS Console Account

Login on https://console.aws.amazon.com/console/home with your AWS account. Without an account or not having logged
AWS connections might not work.

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

The service requires the mappings defined in `ElasticsearchMappings`. They are automatically created during server boot
if they don't exist.


## Automated Tests

TODO


## Local Setup

This Repository contains a sample docker-compose.yaml file which will stand up a fully functional ubirch avatar service including it's dependencies. 
In order to use this you have to export your AWS Access credentials as environment variables as described above (AWS Configuration). Once those are available you can start the containers with ($PWD should be root of the working copy):

		docker-compose up

This will start three container: ElasticSearch, Kibana, ubirch-avatar-service

If you haven not yet install docker-compose follow the instructions found here https://docs.docker.com/compose/install/.

## Create Docker Image

    ./goBuild assembly && ./goBuild containerbuild


## Generate Test Data

Running this removes all your local ElasticSearch indexes and recreates them!!

 1. start server, e.g. in a terminal

    1. set AWS env vars:

        export AWS_ACCESS_KEY_ID={YOUR AWS ACCESS KEY}

        export AWS_SECRET_ACCESS_KEY={YOUR AWS SECRET KEY}

        export MQTT_USER={MQTT-User}

        export MQTT_PASSWORD={MQTT-Password}

    2. if using a terminal, change inside the project folder and

        ./sbt server/run

 2. reset database

     ./sbt "cmdtools/runMain com.ubirch.avatar.cmd.ClearDb"

 3. start test data tool

    1. set AWS env vars:

        export AWS_ACCESS_KEY_ID={YOUR AWS ACCESS KEY}

        export AWS_SECRET_ACCESS_KEY={YOUR AWS SECRET KEY}
        
        export MQTT_USER={MQTT-User}
                
        export MQTT_PASSWORD={MQTT-Password}

    2. if using a terminal, change inside the project folder and

        ./sbt "cmdtools/runMain com.ubirch.avatar.cmd.InitData"

3. now you should find one device "testHans001" and 50 data points
