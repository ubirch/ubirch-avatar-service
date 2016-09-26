packagedArtifacts in file(".") := Map.empty // disable publishing of root/default project

// see http://www.scala-sbt.org/0.13/docs/Parallel-Execution.html for details
concurrentRestrictions in Global := Seq(
  Tags.limit(Tags.Test, 1)
)

lazy val commonSettings = Seq(

  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-feature"),
  organization := "com.ubirch.avatar",

  homepage := Some(url("http://ubirch.com")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/ubirch/ubirch-avatar-service"),
    "scm:git:git@github.com:ubirch/ubirch-avatar-service.git"
  )),
  version := "0.2.0-SNAPSHOT",
  test in assembly := {},
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )

)

/*
 * MODULES
 ********************************************************/

lazy val avatarService = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(server, core, config, modelRest, modelDb, testBase)

lazy val server = project
  .settings(commonSettings: _*)
  .settings(mergeStrategy: _*)
  .dependsOn(core, config, testBase % "test")
  .settings(
    description := "REST interface and Akka HTTP specific code",
    libraryDependencies ++= depServer,
    fork in run := true,
    resolvers ++= Seq(
      resolverSeebergerJson
    ),
    mainClass in(Compile, run) := Some("com.ubirch.avatar.backend.Boot")
  )

lazy val core = project
  .settings(commonSettings: _*)
  .dependsOn(config, modelRest, modelDb)
  .settings(
    description := "business logic",
    libraryDependencies ++= depCore
  )

lazy val config = project
  .settings(commonSettings: _*)
  .settings(
    description := "config code",
    libraryDependencies += ubirchUtilConfig
  )

lazy val modelRest = (project in file("model-rest"))
  .settings(commonSettings: _*)
  .settings(
    name := "model-rest",
    description := "JSON models",
    libraryDependencies ++= depModelRest
  )

lazy val modelDb = (project in file("model-db"))
  .settings(commonSettings: _*)
  .settings(
    name := "model-db",
    description := "DB models",
    libraryDependencies ++= depModelDb
  )

lazy val testBase = (project in file("test-base"))
  .settings(commonSettings: _*)
  .settings(
    name := "test-base",
    description := "test tools",
    libraryDependencies ++= depTestBase
  )

/*
 * MODULE DEPENDENCIES
 ********************************************************/

lazy val depServer = Seq(

  //akka
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaV,

  //testing
  scalatest % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  akkaHttpTestkit % "test",

  // logging
  typesafeScalaLogging,
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "ch.qos.logback" % "logback-core" % "1.1.7",
  "org.slf4j" % "slf4j-api" % "1.7.12",

  ubirchUtilJsonAutoConvert,
  ubirchUtilRestAkkaHttp

)

lazy val depCore = Seq(
  typesafeScalaLogging,
  scalatest % "test"
)

lazy val depModelRest = joda ++ json4s :+ ubirchUtilJsonAutoConvert

lazy val depModelDb = Seq()

lazy val depTestBase = Seq(
  scalatest,
  akkaHttpTestkit,
  ubirchUtilJsonAutoConvert
)

/*
 * DEPENDENCIES
 ********************************************************/

lazy val akkaV = "2.4.10"
lazy val json4sV = "3.4.0"
lazy val scalaTestV = "3.0.0"

lazy val scalatest = "org.scalatest" %% "scalatest" % scalaTestV
lazy val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % akkaV

lazy val typesafeScalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"

lazy val joda = Seq(jodaTime, jodaConvert)
lazy val jodaTime = "joda-time" % "joda-time" % "2.9.4"
lazy val jodaConvert = "org.joda" % "joda-convert" % "1.8"

lazy val json4s = Seq(json4sNative, json4sExt)
lazy val json4sNative = "org.json4s" %% "json4s-native" % json4sV
lazy val json4sExt = "org.json4s" %% "json4s-ext" % json4sV

lazy val ubirchUtilConfig = "com.ubirch.util" %% "config" % "0.1"
lazy val ubirchUtilRestAkkaHttp = "com.ubirch.util" %% "rest-akka-http" % "0.2"
lazy val ubirchUtilJsonAutoConvert = "com.ubirch.util" %% "json-auto-convert" % "0.1"

/*
 * RESOLVER
 ********************************************************/

lazy val resolverSeebergerJson = Resolver.bintrayRepo("hseeberger", "maven")

/*
 * MISC
 ********************************************************/

lazy val mergeStrategy = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList("org", "joda", "time", xs@_*) => MergeStrategy.first
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
    case m if m.toLowerCase.endsWith("application.conf") => MergeStrategy.concat
    case m if m.toLowerCase.endsWith("application.dev.conf") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("application.base.conf") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("logback.xml") => MergeStrategy.first
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
  }
)
