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
      "com.ubirch.chain" %% "config" % "0.0.1-SNAPSHOT"
    )

### `core`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.chain" %% "core" % "0.0-1-SNAPSHOT"
    )
        
### `server`

    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.bintrayRepo("hseeberger", "maven")
    )
    libraryDependencies ++= Seq(
      "com.ubirch.chain" %% "server" % "0.0.1-SNAPSHOT"
    )

## REST Methods

TODO

## Configuration

TODO

## Automated Tests

TODO

## Local Setup

TODO

## create docker image

    ./sbt server/docker
