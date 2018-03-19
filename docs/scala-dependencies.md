## Scala Dependencies

### `aws`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "aws" % "0.4.2-SNAPSHOT"
)
```

### `client`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.bintrayRepo("rick-beton", "maven") // BeeClient
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "client" % "0.4.2-SNAPSHOT"
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
  "com.ubirch.avatar" %% "cmdtools" % "0.4.2-SNAPSHOT"
)
```

### `config`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "config" % "0.4.2-SNAPSHOT"
)
```

### `core`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "core" % "0.4.2-SNAPSHOT"
)
```

### `model-db`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "model-db" % "0.4.2-SNAPSHOT"
)
```

### `model-rest`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "model-rest" % "0.4.2-SNAPSHOT"
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
  "com.ubirch.avatar" %% "server" % "0.4.2-SNAPSHOT"
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
  "com.ubirch.avatar" %% "test-base" % "0.4.2-SNAPSHOT"
)
```

### `util`

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
libraryDependencies ++= Seq(
  "com.ubirch.avatar" %% "util" % "0.4.2-SNAPSHOT"
)
```
