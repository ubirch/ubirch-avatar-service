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
  version := "0.3.11-SNAPSHOT",
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
  .aggregate(server, cmdtools, client, core, aws, config, model, testBase, util)

lazy val server = project
  .settings(commonSettings: _*)
  .settings(mergeStrategy: _*)
  .dependsOn(util, core, config, testBase % "test")
  .enablePlugins(DockerPlugin)
  .settings(
    description := "REST interface and Akka HTTP specific code",
    libraryDependencies ++= depServer,
    fork in run := true,
    resolvers ++= Seq(
      resolverSeebergerJson
    ),
    mainClass in(Compile, run) := Some("com.ubirch.avatar.backend.Boot"),
    resourceGenerators in Compile += Def.task {
      generateDockerFile(baseDirectory.value / ".." / "Dockerfile.input", (assemblyOutputPath in assembly).value)
    }.taskValue
  )

lazy val cmdtools = project
  .settings(commonSettings: _*)
  .dependsOn(core, client, util, testBase)
  .settings(
    description := "command line tools"
  )

lazy val client = project
  .settings(commonSettings: _*)
  .dependsOn(config, model, util)
  .settings(
    description := "REST client for the avatarService",
    libraryDependencies ++= depClient,
    resolvers ++= Seq(
      resolverBeeClient
    )
  )

lazy val core = project
  .settings(commonSettings: _*)
  .dependsOn(config, aws, model, util, testBase % "test")
  .settings(
    description := "business logic",
    libraryDependencies ++= depCore,
    resolvers ++= Seq(
      resolverRoundEights,
      resolverEclipse
    )
  )

lazy val aws = project
  .settings(commonSettings: _*)
  .dependsOn(config, model, util, testBase % "test")
  .settings(
    description := "aws related stuff",
    libraryDependencies ++= depAws
  )

lazy val config = project
  .settings(commonSettings: _*)
  .settings(
    description := "config code",
    libraryDependencies += ubirchConfig
  )

lazy val model = project
  .settings(commonSettings: _*)
  .dependsOn(config)
  .settings(
    name := "model",
    description := "JSON models",
    libraryDependencies ++= depModel
  )

lazy val testBase = (project in file("test-base"))
  .settings(commonSettings: _*)
  .dependsOn(model, config, util)
  .settings(
    name := "test-base",
    description := "test tools",
    libraryDependencies ++= depTestBase,
    resolvers ++= Seq(
      resolverBeeClient,
      resolverRoundEights
    )
  )

lazy val util = project
  .settings(commonSettings: _*)
  .dependsOn(config, model)
  .settings(
    description := "ubirch-avatar-service specific utils",
    libraryDependencies ++= depUtil,
    resolvers ++= Seq(
      resolverBeeClient,
      resolverRoundEights
    )
  )

/*
 * MODULE DEPENDENCIES
 ********************************************************/

lazy val depServer = Seq(

  //akka
  akkaG %% "akka-actor" % akkaV,
  akkaG %% "akka-slf4j" % akkaV,
  akkaG %% "akka-http" % akkaHttpV,

  //testing
  scalatest % "test",

  ubirchJson,
  ubirchJsonAutoConvert,
  ubirchRestAkkaHttp,
  ubirchResponse,
  ubirchOidcUtils

) ++ scalaLogging

lazy val depCore = Seq(
  ubirchElasticsearchClientBinary,
  ubirchCrypto,
  ubirchNotary,
  ubirchResponse,
  spireMath,
  scalatest % "test",
  akkaTestkit % "test"
) ++ akkaCamel ++ scalaLogging

lazy val depClient = Seq(
  beeClient
) ++ scalaLogging

lazy val depAws = Seq(
  ubirchJson,
  ubirchUUID % "test",
  scalatest % "test"
) ++ awsIotSdk ++ awsSqsSdk ++ scalaLogging

lazy val depModel = Seq(
  ubirchJsonAutoConvert,
  json4sNative,
  ubirchUUID
) ++ joda

lazy val depUtil = Seq(
  ubirchCrypto,
  ubirchJson,
  ubirchElasticsearchClientBinary,
  ubirchElasticsearchUtil,
  ubirchUUID % "test",
  scalatest % "test"
) ++ json4s ++ scalaLogging

lazy val depTestBase = Seq(
  scalatest,
  ubirchRestAkkaHttpTest,
  beeClient,
  ubirchUUID,
  ubirchCrypto
) ++ json4s ++ scalaLogging

/*
 * DEPENDENCIES
 ********************************************************/

// VERSIONS
lazy val akkaV = "2.4.17"
lazy val akkaHttpV = "10.0.5"
lazy val json4sV = "3.5.1"
lazy val awsSdkV = "1.11.93"
lazy val camelV = "2.18.1"
lazy val scalaTestV = "3.0.1"
lazy val spireV = "0.13.0"

// GROUP NAMES
lazy val akkaG = "com.typesafe.akka"
lazy val logbackG = "ch.qos.logback"
lazy val json4sG = "org.json4s"
lazy val awsG = "com.amazonaws"
lazy val ubirchUtilG = "com.ubirch.util"

lazy val scalatest = "org.scalatest" %% "scalatest" % scalaTestV
lazy val akkaHttpTestkit = akkaG %% "akka-http-testkit" % akkaHttpV
lazy val akkaTestkit = akkaG %% "akka-testkit" % akkaV

lazy val scalaLogging = Seq(
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2" exclude("org.slf4j", "slf4j-api"),
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0" exclude("org.slf4j", "slf4j-api"),
  "ch.qos.logback" % "logback-core" % "1.1.7",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.internetitem" % "logback-elasticsearch-appender" % "1.4"
)

lazy val akkaCamel = Seq(
  "org.apache.camel" % "camel-core" % camelV,
  "org.apache.camel" % "camel-aws" % camelV,
  "org.apache.camel" % "camel-paho" % camelV,
  "org.apache.camel" % "camel-mqtt" % camelV,
  "com.typesafe.akka" %% "akka-camel" % akkaV exclude("org.apache.camel", "camel-core")
)

lazy val joda = Seq(jodaTime, jodaConvert)
lazy val jodaTime = "joda-time" % "joda-time" % "2.9.4"
lazy val jodaConvert = "org.joda" % "joda-convert" % "1.8.1"

lazy val json4s = Seq(json4sNative, json4sExt, json4sJackson)
lazy val json4sNative = json4sG %% "json4s-native" % json4sV
lazy val json4sExt = json4sG %% "json4s-ext" % json4sV
lazy val json4sJackson = "org.json4s" %% "json4s-jackson" % json4sV

lazy val spireMath = "org.spire-math" %% "spire" % spireV

// list of all available AWS artifacts: https://github.com/aws/aws-sdk-java/blob/master/aws-java-sdk-bom/pom.xml
lazy val awsIotSdk = Seq(
  awsG % "aws-java-sdk-iot" % awsSdkV exclude("joda-time", "joda-time") exclude("com.fasterxml.jackson.core", "jackson-databind") exclude("com.fasterxml.jackson.dataformat", "jackson-dataformat-cbor"),
  awsG % "aws-iot-device-sdk-java" % "1.1.0"
)
lazy val awsSqsSdk = Seq(
  awsG % "aws-java-sdk-sqs" % awsSdkV
)

lazy val beeClient = "uk.co.bigbeeconsultants" %% "bee-client" % "0.29.1"

lazy val excludedLoggers = Seq(
  ExclusionRule(organization = "com.typesafe.scala-logging"),
  ExclusionRule(organization = "org.slf4j"),
  ExclusionRule(organization = "ch.qos.logback")
)

lazy val ubirchConfig = ubirchUtilG %% "config" % "0.1" excludeAll (excludedLoggers: _*)
lazy val ubirchCrypto = ubirchUtilG %% "crypto" % "0.3.3" excludeAll (excludedLoggers: _*)
lazy val ubirchElasticsearchClientBinary = ubirchUtilG %% "elasticsearch-client-binary" % "2.0.1-SNAPSHOT" excludeAll (excludedLoggers: _*)
lazy val ubirchElasticsearchUtil = ubirchUtilG %% "elasticsearch-util" % "2.0.0" excludeAll (excludedLoggers: _*)
lazy val ubirchJson = ubirchUtilG %% "json" % "0.3.4" excludeAll (excludedLoggers: _*)
lazy val ubirchJsonAutoConvert = ubirchUtilG %% "json-auto-convert" % "0.3.4" excludeAll (excludedLoggers: _*)
lazy val ubirchOidcUtils = ubirchUtilG %% "oidc-utils" % "0.2.5" excludeAll (excludedLoggers: _*)

lazy val ubirchNotary = "com.ubirch.notary" %% "client" % "0.3.2" excludeAll (
  excludedLoggers ++ Seq(ExclusionRule(organization = "com.ubirch.util", name = "json-auto-convert")): _*
  )
lazy val ubirchRestAkkaHttp = ubirchUtilG %% "rest-akka-http" % "0.3.4" excludeAll (excludedLoggers: _*)
lazy val ubirchRestAkkaHttpTest = ubirchUtilG %% "rest-akka-http-test" % "0.3.4" excludeAll (excludedLoggers: _*)
lazy val ubirchResponse = ubirchUtilG %% "response-util" % "0.1.3" excludeAll (excludedLoggers: _*)
lazy val ubirchUUID = ubirchUtilG %% "uuid" % "0.1.1" excludeAll (excludedLoggers: _*)

/*
 * RESOLVER
 ********************************************************/

lazy val resolverSeebergerJson = Resolver.bintrayRepo("hseeberger", "maven")
lazy val resolverBeeClient = Resolver.bintrayRepo("rick-beton", "maven")
lazy val resolverRoundEights = "RoundEights" at "http://maven.spikemark.net/roundeights"
lazy val resolverEclipse = "eclipse-paho" at "https://repo.eclipse.org/content/repositories/paho-releases"

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
    case m if m.toLowerCase.endsWith("logback-test.xml") => MergeStrategy.discard
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
  }
)

def generateDockerFile(file: File, jarFile: sbt.File): Seq[File] = {
  val contents =
    s"""SOURCE=server/target/scala-2.11/${jarFile.getName}
       |TARGET=${jarFile.getName}
       |""".stripMargin
  IO.write(file, contents)
  Seq(file)
}
