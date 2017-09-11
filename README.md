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

this content has been moved to a separate file: _docs/release-history.md_

## Scala Dependencies

### `aws`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "aws" % "0.3.26"
)
```

### `client`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("rick-beton", "maven") // BeeClient
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "client" % "0.3.26"
)
```

#### Configuration
   
| Config Item                                      | Mandatory  | Description                                                |
|:-------------------------------------------------|:-----------|:-----------------------------------------------------------|
| ubirchAvatarService.client.rest.baseUrl          | no         | avatar-service base url (default = http://localhost:8080)  |
| ubirchAvatarService.client.rest.timeout.connect  | no         | timeout during connection creation in milliseconds (default = 15000 ms) |
| ubirchAvatarService.client.rest.timeout.read     | no         | timeout when reading from server in milliseconds (default = 15000 ms)   |

#### Usage

See `com.ubirch.avatar.cmd.ImportTrackle` for an example usage.

### `cmdtools`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "cmdtools" % "0.3.26"
)
```

### `config`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "config" % "0.3.26"
)
```

### `core`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "core" % "0.3.26"
)
```

### `model-db`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "model-db" % "0.3.26"
)
```

### `model-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "model-rest" % "0.3.26"
)
```

### `server`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("hseeberger", "maven"),
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "server" % "0.3.26"
)
```

### `test-base`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayRepo("hseeberger", "maven"),
  Resolver.bintrayRepo("rick-beton", "maven") // BeeClient
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "test-base" % "0.3.26"
)
```

### `util`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "util" % "0.3.26"
)
```


## REST Methods

this content has been moved to a separate file: _docs/rest-methods.md_


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

*Running `dev-scripts/resetDatabase.sh` does everything in this step.*

     ./sbt "cmdtools/runMain com.ubirch.avatar.cmd.ClearDb"

 3. start test data tool

    1. set AWS env vars:

        ```bash
        export AWS_ACCESS_KEY_ID={YOUR AWS ACCESS KEY}
        export AWS_SECRET_ACCESS_KEY={YOUR AWS SECRET KEY}
        export MQTT_USER={MQTT-User}
        export MQTT_PASSWORD={MQTT-Password}
        ```

    2. if using a terminal, change inside the project folder and

*Running `dev-scripts/initData.sh` does everything in this step.*

        ```bash
        ./sbt "cmdtools/runMain com.ubirch.avatar.cmd.InitData"
        ```

3. now you should find one device "testHans001" and 50 data points

## Import Trackle Data

1. Prepare User

    The test data generation includes the generation of test data and a device. This means we still need a user which you'll
    have to create/register manually by logging in on the AdminUI of the remote environment. Please remember the token
    resulting from the registration or login.

1. Prepare Data Import

    ```bash
    # user token from registration or login
    export AVATAR_USER_TOKEN=token-12345678
    # (optional) base url of the remote environment's avatar-service (defaults to http://localhost:8080)
    export AVATAR_BASE_URL=https://avatar.myserver.com:8080
    ```

1. Run Data Import

You can also run `dev-scripts/importTrackle.sh $AVATAR_USER_TOKEN`.

    ```bash
    ./sbt "cmdtools/runMain com.ubirch.avatar.cmd.ImportTrackle"
    ```
