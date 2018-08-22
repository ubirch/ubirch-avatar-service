## REST Methods

### Welcome / Health / Check

    curl localhost:8080/
    curl localhost:8080/api/avatarService/v1
    curl localhost:8080/api/avatarService/v1/check

If server is healthy response is:

    200 {"version":"1.0","status":"OK","message":"Welcome to the ubirchAvatarService  ( $GO_PIPELINE_NAME / $GO_PIPELINE_LABEL / $GO_PIPELINE_REVISION )"}

### Deep Check / Server Health

    curl localhost:8080/api/avatarService/v1/deepCheck

If healthy the response is:

    200 {"status":true,"messages":[]}

If not healthy the status is `false` and the `messages` array not empty:

    503 {"status":false,"messages":["unable to connect to the database"]}


### Device CRUD

#### LIST all devices

returns an array of all devices the authenticated user has connected

    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XGET localhost:8080/api/avatarService/v1/device -H "Authorization: Bearer $TOKEN"

to list device short infos use the stub endpoint

    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XGET localhost:8080/api/avatarService/v1/device/stub -H "Authorization: Bearer $TOKEN"


#### CREATE device

creates a new device

    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XPOST localhost:8080/api/avatarService/v1/device -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" -d '{
        "deviceId": "5df0c9b7-564a-4b90-8f1b-998fbe1a1cbf",
        "owners": [
            "fb5886c2-7ebd-45c0-b5a5-fc100d7ae966"
        ],
        "groups": [
            "913e26f2-d0ac-4735-9535-df12265aca8b"
        ],
        "deviceTypeKey": "lightsLamp",
        "deviceName": "new device",
        "hwDeviceId": "07a1d44f-4c1f-4863-8a39-1440e94476f6",
        "hashedHwDeviceId": "nNeA0pqvk2jFw+sGFpTUBzAT5Jpk54Pi1QffnL5mtR3U8Zs6iyPRT5AxTPxtIgwEDTr65i3H84jphnpLBLVwtg==", // base64(sha512(hwDeviceId))
        "tags": [
            "ubirch#0",
            "actor","btcDemo"
        ],
        "deviceConfig": { // optional
            "i":900,
            "bf":0
        },
        "deviceProperties": {}, // optional
        "subQueues": ["mySubQueue1", "mySubQueue2"] // optional
        "pubQueues": ["myPubQueue1", "myPubQueue2"] // optional
        "pubRawQueues": ["myPubRawQueue1", "myPubRawQueue2"] // optional
        "avatarLastUpdated": "2018-08-21T15:56:246.512Z", // optional
        "deviceLastUpdated": "2018-08-21T15:56:246.512Z", // optional
        "updated": "2018-08-21T15:56:246.512Z", // optional
        "created": "2018-08-21T15:54:46.271Z"
    }'


#### CREATE, READ, UPDATE, DELETE device with ID

READ device with given id

    /api/avatarService/v1/device/<DEVICE_ID> // uri path format
    
    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XGET localhost:8080/api/avatarService/v1/device/5df0c9b7-564a-4b90-8f1b-998fbe1a1cbf -H "Authorization: Bearer $TOKEN"

CREATE device with given id

*Note* the deviceId in the uri path is being ignored.

    /api/avatarService/v1/device/<DEVICE_ID> // uri path format

    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XPOST localhost:8080/api/avatarService/v1/device/5df0c9b7-564a-4b90-8f1b-998fbe1a1cbf -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{
        "deviceId": "5df0c9b7-564a-4b90-8f1b-998fbe1a1cbf",
        "owners": [
            "fb5886c2-7ebd-45c0-b5a5-fc100d7ae966"
        ],
        "groups": [
            "913e26f2-d0ac-4735-9535-df12265aca8b"
        ],
        "deviceTypeKey": "lightsLamp",
        "deviceName": "new device",
        "hwDeviceId": "07a1d44f-4c1f-4863-8a39-1440e94476f6",
        "hashedHwDeviceId": "nNeA0pqvk2jFw+sGFpTUBzAT5Jpk54Pi1QffnL5mtR3U8Zs6iyPRT5AxTPxtIgwEDTr65i3H84jphnpLBLVwtg==", // base64(sha512(hwDeviceId))
        "tags": [
            "ubirch#0",
            "actor","btcDemo"
        ],
        "deviceConfig": { // optional
            "i":900,
            "bf":0
        },
        "deviceProperties": {}, // optional
        "subQueues": ["mySubQueue1", "mySubQueue2"] // optional
        "pubQueues": ["myPubQueue1", "myPubQueue2"] // optional
        "pubRawQueues": ["myPubRawQueue1", "myPubRawQueue2"] // optional
        "avatarLastUpdated": "2018-08-21T15:56:246.512Z", // optional
        "deviceLastUpdated": "2018-08-21T15:56:246.512Z", // optional
        "updated": "2018-08-21T15:56:246.512Z", // optional
        "created": "2018-08-21T15:54:46.271Z"
    }'

UPDATE device with given id

    /api/avatarService/v1/device/<DEVICE_ID> // uri path format

    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XPUT localhost:8080/api/avatarService/v1/device/5df0c9b7-564a-4b90-8f1b-998fbe1a1cbf -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{
        "deviceId": "5df0c9b7-564a-4b90-8f1b-998fbe1a1cbf",
        "owners": [
            "fb5886c2-7ebd-45c0-b5a5-fc100d7ae966"
        ],
        "groups": [
            "913e26f2-d0ac-4735-9535-df12265aca8b"
        ],
        "deviceTypeKey": "lightsLamp",
        "deviceName": "new device",
        "hwDeviceId": "07a1d44f-4c1f-4863-8a39-1440e94476f6",
        "hashedHwDeviceId": "nNeA0pqvk2jFw+sGFpTUBzAT5Jpk54Pi1QffnL5mtR3U8Zs6iyPRT5AxTPxtIgwEDTr65i3H84jphnpLBLVwtg==", // base64(sha512(hwDeviceId))
        "tags": [
            "ubirch#0",
            "actor","btcDemo"
        ],
        "deviceConfig": { // optional
            "i":900,
            "bf":0
        },
        "deviceProperties": {}, // optional
        "subQueues": ["mySubQueue1", "mySubQueue2"] // optional
        "pubQueues": ["myPubQueue1", "myPubQueue2"] // optional
        "pubRawQueues": ["myPubRawQueue1", "myPubRawQueue2"] // optional
        "avatarLastUpdated": "2018-08-21T15:56:246.512Z", // optional
        "deviceLastUpdated": "2018-08-21T15:56:246.512Z", // optional
        "updated": "2018-08-21T15:56:246.512Z", // optional
        "created": "2018-08-21T15:54:46.271Z"
      }'

DELETE device with given id (idempotent)

    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XDELETE localhost:8080/api/avatarService/v1/device/<DEVICE_ID> -H "Authorization: Bearer $TOKEN"

#### Device State

get state of device with given id

    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XGET localhost:8080/api/avatarService/v1/device/<DEVICE_ID>/state -H "Authorization: Bearer $TOKEN"

to list a particular device's short info use the stub endpoint

    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XGET localhost:8080/api/avatarService/v1/device/stub/<DEVICE_ID> -H "Authorization: Bearer $TOKEN"

#### Device Claim

    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XPUT localhost:8080/api/avatarService/v1/device/claim -H "Authorization: Bearer $TOKEN"

### Device Data

#### Raw

Raw data comes directly from devices in a machine readable format. The server will relay it to a transformer which will
then typically convert it into something more human readable.

There's a lot more fields compared to what's been used in the example below. For more details please refer to the
scaladoc of the `DeviceDataRaw` object.

    curl -XPOST localhost:8080/api/avatarService/v1/device/data/raw -H "Content-Type: application/json" -d '{
        "id": "8aa3d0ec-9ec8-4785-93e9-6fd1705dace6",
        "v": "0.0.3",
        "a": "nNeA0pqvk2jFw+sGFpTUBzAT5Jpk54Pi1QffnL5mtR3U8Zs6iyPRT5AxTPxtIgwEDTr65i3H84jphnpLBLVwtg==",
        "ts": "2016-06-30T11:39:51Z",
        "p": {
            "foo": 23,
            "bar": "ubirch-sensor-data"
        },
        "config": {
            "i":1900,
            "bf":0
        },
        "deviceType": "lightsLamp",
        "tags": [
            "ubirch#0",
            "actor"
        ]
    }'

#### Update

TODO: add description

    curl -XPOST localhost:8080/api/avatarService/v1/device/update -H "Content-Type: application/json" -d '{
        "id": "8aa3d0ec-9ec8-4785-93e9-6fd1705dace6",
        "v": "0.0.3",
        "a": "nNeA0pqvk2jFw+sGFpTUBzAT5Jpk54Pi1QffnL5mtR3U8Zs6iyPRT5AxTPxtIgwEDTr65i3H84jphnpLBLVwtg==",
        "ts": "2016-06-30T11:39:51Z",
        "p": {
            "foo": 23,
            "bar": "ubirch-sensor-data"
        },
        "config": {
            "i":1900,
            "bf":0
        },
        "deviceType": "lightsLamp",
        "tags": [
            "ubirch#0",
            "actor"
        ]
    }'

#### Bulk

TODO: add description

    curl -XPOST localhost:8080/api/avatarService/v1/device/update/bulk -H "Content-Type: application/json" -d '{
        "id": "8aa3d0ec-9ec8-4785-93e9-6fd1705dace6",
        "v": "0.0.3",
        "a": "nNeA0pqvk2jFw+sGFpTUBzAT5Jpk54Pi1QffnL5mtR3U8Zs6iyPRT5AxTPxtIgwEDTr65i3H84jphnpLBLVwtg==",
        "ts": "2016-06-30T11:39:51Z",
        "p": {
            "foo": 23,
            "bar": "ubirch-sensor-data"
        },
        "config": {
            "i":1900,
            "bf":0
        },
        "deviceType": "lightsLamp",
        "tags": [
            "ubirch#0",
            "actor"
        ]
    }'

#### MessagePack

Update a device just as with `/device/update` but with MessagePack as input format. With the boolean parameter `js` we
can tell the server if we want it to return JSON or MessagePack.

    // TODO add valid MessagePack payload
    curl -XPOST localhost:8080/api/avatarService/v1/device/update/mpack?js=true -d ''

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

    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XGET localhost:8080/api/avatarService/v1/device/deviceType -H "Authorization: Bearer $TOKEN"

#### Create device type

    curl -XPOST localhost:8080/api/avatarService/v1/device/deviceType -H "Content-Type: application/json" -d '{
        "key": "trackle",
        "name": {
          "de": "Trackle",
          "en": "Trackle"
        },
        "icon": "trackle",
        "displayKeys": [],
        "transformerQueue": "transformer_queue_to_send_to_for_data_tranformation", // optional
        "defaults": {
          "properties": {}, // any valid json
          "config": { // any valid json
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

    {"key": "trackle", "name": {"de": "Trackle", "en": "Trackle"}, "icon": "trackle", "displayKeys": [], "transformerQueue": "transformer_queue_to_send_to_for_data_tranformation", "defaults": {"properties": {}, "config": {"i": 60}, "tags": ["ubirch#1", "actor", "trackle"]}}

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
        "displayKeys": [],
        "transformerQueue": "transformer_queue_to_send_to_for_data_tranformation", // optional
        "defaults": {
          "properties": {}, // any valid json
          "config": { // any valid json
          "i": 120
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
    Date: Thu, 10 Nov 2016 16:33:24 GMT
    Content-Type: application/json
    Content-Length: 159

    {"key": "trackle", "name": {"de": "Trackle", "en": "Trackle"}, "icon": "trackle", "displayKeys": [], "transformerQueue": "transformer_queue_to_send_to_for_data_tranformation", "defaults": {"properties": {}, "config": {"i": 120}, "tags": ["ubirch#1", "actor", "trackle"]}}

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

    # to make the call with a ubirch token use `-H "Authorization: $TOKEN"` instead
    curl -XGET localhost:8080/api/avatarService/v1/device/deviceType/init -H "Authorization: Bearer $TOKEN"
    