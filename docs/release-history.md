## Release History

### Version 0.4.0 (tbd)

* refactored actor instantiating
* fixed pubKey checking
* update to `com.ubirch.util:oidc-utils:0.5.2`
* update to `com.ubirch.util:redis-util:0.3.5`
* refactored `AvatarRestClient` to accept ubirch tokens, too (previously all tokens were used as if they're OIDC tokens) 

### Version 0.3.30 (2018-03-13)

* added `AvatarRestClient.deviceGET()`
* added `AvatarRestClient.deviceIdPUT()`

### Version 0.3.29 (2018-03-08)

* update to `com.ubirch.util:config:0.2.0`
* update to `com.ubirch.util:elasticsearch-client-binary:2.3.5`
* update to `com.ubirch.util:mongo-utils:0.3.7`
* update to `com.ubirch.util:mongo-test-utils:0.3.7`
* update to `com.ubirch.util:oidc-utils:0.4.15`
* update to `com.ubirch.user:client-rest:0.7.0`
* update to `com.ubirch.key:client-rest:0.2.2`

### Version 0.3.27 (2017-10-16)

* `DeviceManager.create()` automatically converts _hwDeviceId_ to lower case
* `DeviceManager.create()` checks that the deviceId and hwDeviceId don't exist already
* `DeviceManager.update()` no longer allows updating the following device fields: _created_, _deviceId_, _hwDeviceId_ or _hashedHwDeviceId_
* fixed handling of faulty `Future` handling in `DeviceIdRoute`
* `DeviceManager.update()` always stores _hwDeviceId_ as lower case
* update to `com.ubirch.user:client-rest:0.6.3`
* reduce code duplication (`DeviceUtil.createKeyPair` was already implemented by `EccUtil.generateEccKeyPair`)
* switch to refactored `CamelActorUtil` from `com.ubirch.util:camel-utils`

### Version 0.3.26 (2017-09-11)

* add optional docker env var _ES_LARGE_PAGE_SIZE_

### Version 0.3.25 (2017-08-21)

* add missing Elasticsearch mappings

### Version 0.3.24 (2017-08-11)

* add field `owners: Set[UUID]` to `Device`
* update `DummyDevices` to sometimes set a random ownerId
* update to `com.ubirch.user:client-rest:0.6.2`
* create device with ownerId being set automatically 

### Version 0.3.23 (2017-08-10)

* update to `com.ubirch.util:oidc-utils:0.4.11`
* update to `com.ubirch.user:client-rest:0.6.1`

### Version 0.3.22 (2017-07-31)

* revert to `com.ubirch.util:elasticsearch-client-binary:2.1.0`
* revert to `com.ubirch.util:elasticsearch-util:2.1.

### Version 0.3.21 (2017-07-31)

* fixed broken logging (would lead to a Throwable not being logged)
* update to `com.ubirch.util:elasticsearch-client-binary:2.1.0`
* update to `com.ubirch.util:elasticsearch-util:2.1.0`
* update to `com.ubirch.util:mongo-utils:0.3.6`
* update to `com.ubirch.util:mongo-test-utils:0.3.6`
* update to `com.ubirch.util:oidc-utils:0.4.10`
* update to `com.ubirch.user:client-rest:0.5.1`

### Version 0.3.20 (2017-07-28)

* improve endpoint documentation
* update to `com.ubirch.key:client-rest:0.2.0`
* update to `com.ubirch.user:client-rest:0.5.0`
* deepCheck() includes the key-service deepCheck now
* deepCheck() includes the user-service deepCheck now
* deepCheck() includes a MongoDB connectivity check now
* deepCheck() includes a Redis connectivity check now

### Version 0.3.19 (2017-07-27)

* update to `com.ubirch.user:client-rest:0.4.19`
* update to `com.ubirch.key:client-rest:0.1.13`

### Version 0.3.18 (sbt)

* refactor where REST client connection timeouts are configured
* less logging in `AvatarRestClient`
* add method `AvatarRestClient.deviceStubGET`
* update to `com.ubirch.util:json:0.4.3`
* update to `com.ubirch.util:oidc-utils:0.4.9`
* update to `com.ubirch.util:elasticsearch-client-binary:2.0.8`
* update to `com.ubirch.util:mongo-utils:0.3.5`
* update to `com.ubirch.util:mongo-test-utils:0.3.5`
* update to `com.ubirch.util:response-util:0.2.4`
* update to `com.ubirch.util:oidc-utils:0.4.9`

### Version 0.3.17 (2017-07-25)

* refactored `ImportTrackle` to work with remote environments, too (e.g. avatar-svc running in dev or demo environment)
* add route `POST /api/avatarService/v1/device/update` (accepting JSON)
* add route `POST /api/avatarService/v1/device/bulk` (accepting JSON)

### Version 0.3.16 (2017-07-25)

* update Akka HTTP to 10.0.9
* update to `com.ubirch.util:rest-akka-http(-test):0.3.8`
* update to `com.ubirch.util:response-util:0.2.3`
* update to `com.ubirch.util:oidc-utils_:0.4.8`
* update to `com.ubirch.util:mongo(-test)-utils:0.3.4`
* update to Akka 2.4.19
* update to `com.ubirch.key:client-rest:0.1.12` (REST client based on Akka HTTP instead of PlayWS)
* update to `com.ubirch.user:*:0.4.18` (REST client based on Akka HTTP instead of PlayWS)

### Version 0.3.15 (2017-07-13)

* fixed problem with faulty log4j logging by adding the dependency `log4j-over-slf4j`
* add `MongoConstraints`
* introduced `MongoStorageCleanup`
* improved `MongoSpec`
* added clean up of MongoDD to `ClearDb`
* MongoDB constraints are now created during server start
* update _com.ubirch.util:mongo(-test)-utils_ to 0.3.3
* update _com.ubirch.user:*_ to 0.4.14

### Version 0.3.14 (2017-07-11)

* bugfix: it was possible to create two devices with the same hwDeviceId

### Version 0.3.13 (2017-06-29)

* add scripts `dev-scripts/resetDatabase.sh` and `dev-scripts/initData.sh`
* updated to _com.ubirch.util:json:0.4.2_ and all ubirch util libs depending on it, too
* update to _com.ubirch.user:client-rest:0.4.13_

### Version 0.3.12 (2017-06-22)

* AvatarState is no longer stored in AWS IoT but instead in a MongoDB
* update to _json4s_ 3.5.2
* update _com.ubirch.util:json_ to 0.4.1
* update _com.ubirch.util:elasticsearch-client-binary_ to 2.0.6
* update _com.ubirch.util:mongo-test-utils_ to 0.3.1
* update _com.ubirch.util:mongo-utils_ to 0.3.1
* update _com.ubirch.util:oidc-utils_ to 0.4.6
* update _com.ubirch.util:response-util_ to 0.2.1
* update _com.ubirch.user:client-rest_ to 0.4.10
* bugfix (UBI-264): updates on Azure's CosmosDB show the behavior of an upsert()

### Version 0.3.11 (2017-06-16)

* update to _json4s_ 3.5.1
* update to _de.heikoseeberger:akka-http-json4s_ 1.14.0
* update Akka HTTP to 10.0.6
* update _com.ubirch.notary:notary-client_ to 0.3.2
* update _com.ubirch.util:elasticsearch-util_ to 2.0.0
* update to Elasticsearch 5.3
* update mappings to Elasticsearch 5.3
* rename module _model_ to _model-rest_ and introduce module _model-db_
* update _com.ubirch.util:rest-akka-http_ to 0.3.7
* update _com.ubirch.util:rest-akka-http-test_ to 0.3.7
* creating devices now stores groups as queried from user-service
* update to Akka 2.4.18
* update to Akka HTTP 10.0.6
* creating a device remembers the user's groups (new field _Device.groups_)
* change GO CI related environment variables to: _GO_PIPELINE_NAME_AVATAR_, _GO_PIPELINE_LABEL_AVATAR_ and _GO_REVISION_AVATAR_
* introduce new endpoint: `/api/avatarService/v1/check`
* update _com.ubirch.util:json_ to 0.4.0
* introduce endpoint `/api/avatarService/v1/deepCheck`
* update _com.ubirch.util:elasticsearch-client-binary_ to 2.0.5
* update _com.ubirch.util:response-util_ to 0.2.0
* update _com.ubirch.util:oidc-utils_ to 0.4.5
* update _com.ubirch.user:client-rest_ to 0.4.9

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
