# ubirch-avatar-service

ubirch device-configuration and -dataflow service

## Run the service

by executing:

    sbt server/run

you can also enter the sbt shell and enter:

    clean
    compile
    project server
    run

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


### SBT Dependency Graphs

To browse the dependency graph, this project uses the plugin sbt-dependency-graph 
(https://github.com/jrudolph/sbt-dependency-graph)

To browse e.g. the dependencyGraph for each module use the command 'dependencyBrowseTree' in the sbt shell.

## Scala Dependencies

this content has been moved to a separate file: _docs/release-history.md_


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

To run the tests, first run the docker-compose file in local-test-setup/run-unit-tests and then start the tests.

```bash
cd local-test-setup/run-unit-tests
docker-compose up 
cd ../..
sbt test
```

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
