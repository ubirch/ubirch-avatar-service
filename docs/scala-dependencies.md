## Scala Dependencies

### `aws`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "aws" % "0.6.0-SNAPSHOT"
)
```

### `client-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "client-rest" % "0.6.0-SNAPSHOT"
)
```

#### Configuration
   
| Config Item                           | Mandatory  | Description          |
|:--------------------------------------|:-----------|:---------------------|
| ubirchAvatarService.client.rest.host  | yes        | avatar-service host  |

#### Usage

See `com.ubirch.avatar.client.rest.TemplateServiceClientRestSpec` for an example usage.

The REST client class is `AvatarSvcClientRest` and the host it connects to needs to be configured:

    ubirchAvatarService.client.rest.host = "http://localhost:8080"

It depends on a `akka-http` client. Please refer to the setup of `AvatarSvcClientRestSpec` for further details.

### `client`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("rick-beton", "maven") // BeeClient
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "client" % "0.6.0-SNAPSHOT"
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
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "cmdtools" % "0.6.0-SNAPSHOT"
)
```

### `config`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "config" % "0.6.0-SNAPSHOT"
)
```

### `core`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "core" % "0.6.0-SNAPSHOT"
)
```

### `model-db`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "model-db" % "0.6.0-SNAPSHOT"
)
```

### `model-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "model-rest" % "0.6.0-SNAPSHOT"
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
  "com.ubirch.avatar" %% "server" % "0.6.0-SNAPSHOT"
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
  "com.ubirch.avatar" %% "test-base" % "0.6.0-SNAPSHOT"
)
```

### `util`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "util" % "0.6.0-SNAPSHOT"
)
```
