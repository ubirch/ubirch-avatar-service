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
      "com.ubirch.avatar" %% "config" % "0.0.1-SNAPSHOT"
    )

### `core`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.avatar" %% "core" % "0.0-1-SNAPSHOT"
    )

### `model-rest`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.avatar" %% "model-rest" % "0.0-1-SNAPSHOT"
    )

### `model-db`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.avatar" %% "model-db" % "0.0-1-SNAPSHOT"
    )
        
### `server`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.bintrayRepo("hseeberger", "maven")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.avatar" %% "server" % "0.0.1-SNAPSHOT"
    )
        
### `server`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.avatar" %% "model" % "0.0.1-SNAPSHOT"
    )

## REST Methods

### Welcome / Health

    curl localhost:8080/

If server is healthy response is:

    200 {"version":"1.0","status":"OK","message":"Welcome to the ubirchChainServer"}

### TODO: title

TODO: description

    curl -XGET localhost:8080/api/v1/avatarService/device

    curl -XPOST localhost:8080/api/v1/avatarService/device

### TODO: title

TODO: description

    curl -XGET localhost:8080/api/v1/avatarService/device/<DEVICE_ID>

    curl -XPOST localhost:8080/api/v1/avatarService/device/<DEVICE_ID>

    curl -XDELETE localhost:8080/api/v1/avatarService/device/<DEVICE_ID>

### TODO: title

TODO: description

    curl -XGET localhost:8080/api/v1/avatarService/device/stub/<DEVICE_ID>

## Configuration

TODO

## Automated Tests

TODO

## Local Setup

TODO

## create docker image

    ./sbt server/docker
