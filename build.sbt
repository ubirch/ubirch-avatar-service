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
  version := "0.0.1-SNAPSHOT",
  test in assembly := {},
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots")
  )

)

/*
 * MODULES
 ********************************************************/

lazy val avatarService = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(server, core, config)

lazy val server = project
  .settings(commonSettings: _*)
  .settings(mergeStrategy: _*)
  .dependsOn(core, config)
  .settings(
    libraryDependencies ++= depServer,
    fork in run := true,
    resolvers ++= Seq(
      resolverSeebergerJson
    ),
    mainClass in(Compile, run) := Some("com.ubirch.avatar.backend.Boot")
  )

lazy val core = project
  .settings(commonSettings: _*)
  .dependsOn(config, model)
  .settings(
    libraryDependencies ++= depCore
  )

lazy val config = project
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += ubirchUtilConfig
  )

lazy val model = project
  .settings(commonSettings: _*)
  .settings(

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
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "org.slf4j" % "slf4j-api" % "1.7.12",

  ubirchUtilJsonAutoConvert,
  ubirchUtilRestAkkaHttp

)

lazy val depCore = Seq(
  typesafeScalaLogging,
  scalatest % "test"
)

/*
 * DEPENDENCIES
 ********************************************************/

val akkaV = "2.4.10"
val scalaTestV = "3.0.0"
val ubirchUtilJsonAutoConvertV = "0.1-SNAPSHOT"

lazy val scalatest = "org.scalatest" %% "scalatest" % scalaTestV
lazy val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % akkaV

lazy val typesafeScalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"

lazy val ubirchUtilConfig = "com.ubirch.util" %% "config" % "0.1-SNAPSHOT"
lazy val ubirchUtilRestAkkaHttp = "com.ubirch.util" %% "rest-akka-http" % "0.1-SNAPSHOT"
lazy val ubirchUtilJsonAutoConvert = "com.ubirch.util" %% "json-auto-convert" % ubirchUtilJsonAutoConvertV

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
