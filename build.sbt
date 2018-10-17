// see http://www.scala-sbt.org/0.13/docs/Parallel-Execution.html for details
concurrentRestrictions in Global := Seq(
  Tags.limit(Tags.Test, 1)
)

val commonSettings = Seq(

  scalaVersion := "2.11.12",
  scalacOptions ++= Seq("-feature"),
  organization := "com.ubirch.avatar",
  homepage := Some(url("http://ubirch.com")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/ubirch/ubirch-avatar-service"),
    "scm:git:git@github.com:ubirch/ubirch-avatar-service.git"
  )),
  version := "0.6.0-SNAPSHOT",
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
  .settings(
    commonSettings ++ Seq(packagedArtifacts := Map.empty),
    skip in publish := true
  )
  .aggregate(
    aws,
    client,
    cmdtools,
    config,
    core,
    modelDb,
    modelRest,
    server,
    testBase,
    testTools,
    util
  )

lazy val server = project
  .settings(
    commonSettings,
    mergeStrategy
  )
  .dependsOn(
    config,
    core,
    util,
    testBase % "test",
    testTools % "test"
  )
  .enablePlugins(DockerPlugin)
  .settings(
    description := "REST interface and Akka HTTP specific code",
    libraryDependencies ++= depServer,
    fork in run := true,
    resolvers ++= Seq(
      resolverSeebergerJson,
      resolverTypesafeReleases,
      resolverVelvia
    ),
    mainClass in(Compile, run) := Some("com.ubirch.avatar.backend.Boot"),
    resourceGenerators in Compile += Def.task {
      generateDockerFile(baseDirectory.value / ".." / "Dockerfile.input", (assemblyOutputPath in assembly).value)
    }.taskValue
  )

lazy val cmdtools = project
  .settings(commonSettings)
  .dependsOn(core, client, util, testBase)
  .settings(
    description := "command line tools"
  )

lazy val client = project
  .settings(commonSettings)
  .dependsOn(config, modelRest, util)
  .settings(
    description := "REST client for the avatarService",
    libraryDependencies ++= depClient,
    resolvers ++= Seq(
      resolverBeeClient
    )
  )

lazy val core = project
  .settings(commonSettings)
  .dependsOn(config, aws, modelDb, modelRest, util, testBase % "test")
  .settings(
    description := "business logic",
    libraryDependencies ++= depCore,
    resolvers ++= Seq(
      resolverEclipse,
      resolverElasticsearch
    )
  )

lazy val aws = project
  .settings(commonSettings)
  .dependsOn(config, modelRest, util, testBase % "test")
  .settings(
    description := "aws related stuff",
    libraryDependencies ++= depAws
  )

lazy val config = project
  .settings(commonSettings)
  .settings(
    description := "config code",
    libraryDependencies ++= depConfig
  )

lazy val modelDb = (project in file("model-db"))
  .settings(commonSettings)
  .dependsOn(config)
  .settings(
    name := "model-db",
    description := "database models",
    libraryDependencies ++= depModelDb
  )

lazy val modelRest = (project in file("model-rest"))
  .settings(commonSettings)
  .dependsOn(config)
  .settings(
    name := "model-rest",
    description := "JSON models",
    libraryDependencies ++= depModelRest
  )

lazy val testBase = (project in file("test-base"))
  .settings(commonSettings)
  .dependsOn(modelDb, modelRest, config, util)
  .settings(
    name := "test-base",
    description := "test tools",
    libraryDependencies ++= depTestBase,
    resolvers ++= Seq(
      resolverBeeClient
    )
  )

lazy val testTools = (project in file("test-tools"))
  .settings(commonSettings)
  .dependsOn(core)
  .settings(
    name := "test-tools",
    description := "test tools for use outside of core"
  )

lazy val util = project
  .settings(commonSettings)
  .dependsOn(config, modelDb, modelRest)
  .settings(
    description := "ubirch-avatar-service specific utils",
    libraryDependencies ++= depUtil,
    resolvers ++= Seq(
      resolverBeeClient,
      resolverElasticsearch
    )
  )

/*
 * MODULE DEPENDENCIES
 ********************************************************/

lazy val depServer = Seq(

  //testing
  scalatest % "test",

  ubirchJson,
  ubirchRestAkkaHttp,
  ubirchResponse,
  ubirchOidcUtils

) ++ akka ++ akkaCamel ++ prometheus ++ constructr ++ scalaLogging

lazy val depConfig = Seq(
  ubirchCamelUtils,
  ubirchConfig
)

lazy val depCore = Seq(
  ubirchElasticsearchClientBinary,
  ubirchCamelUtils,
  ubirchCrypto,
  ubirchMongo,
  ubirchNotary,
  ubirchResponse,
  ubirchKeyClientRest,
  ubirchUserClientRest,
  ubirchChainModel,
  ubirchUtilRedisUtil,
  spireMath,
  msgpackScala,
  guava,
  scalatest % "test",
  akkaTestkit % "test"
) ++ akka ++ prometheus ++ akkaCamel ++ scalaLogging

lazy val depClient = Seq(
  beeClient
) ++ scalaLogging

lazy val depAws = Seq(
  ubirchJson,
  ubirchUUID % "test",
  scalatest % "test"
) ++ awsSqsSdk ++ scalaLogging

lazy val depModelDb = Seq(
  json4sNative,
  ubirchUUID
) ++ joda

lazy val depModelRest = Seq(
  json4sNative,
  ubirchUUID
) ++ joda

lazy val depUtil = Seq(
  ubirchCrypto,
  ubirchJson,
  ubirchElasticsearchClientBinary,
  ubirchElasticsearchUtil,
  ubirchMongo,
  ubirchOidcUtils,
  ubirchUUID % "test",
  scalatest % "test"
) ++ json4s ++ scalaLogging

lazy val depTestBase = Seq(
  scalatest,
  ubirchMongoTest,
  ubirchRestAkkaHttpTest,
  beeClient,
  ubirchUUID,
  ubirchCrypto
) ++ json4s ++ scalaLogging

/*
 * DEPENDENCIES
 ********************************************************/

// VERSIONS
val akkaV = "2.5.11"
val akkaHttpV = "10.1.3"
val json4sV = "3.6.0"
val awsSdkV = "1.11.368"
val camelV = "2.22.0"
val scalaTestV = "3.0.5"
val spireV = "0.13.0"
val logbackV = "1.2.3"
val logstashEncV = "5.0"
val slf4jV = "1.7.25"
val log4jV = "2.9.1"
val scalaLogV = "3.7.2"
val scalaLogSLF4JV = "2.1.2"

// GROUP NAMES
val akkaG = "com.typesafe.akka"
val logbackG = "ch.qos.logback"
val json4sG = "org.json4s"
val awsG = "com.amazonaws"
val ubirchUtilG = "com.ubirch.util"

val scalatest = "org.scalatest" %% "scalatest" % scalaTestV
val akkaHttpTestkit = akkaG %% "akka-http-testkit" % akkaHttpV
val akkaTestkit = akkaG %% "akka-testkit" % akkaV

val scalaLogging = Seq(
  "org.slf4j" % "slf4j-api" % slf4jV,
  "org.slf4j" % "log4j-over-slf4j" % slf4jV,
  "org.slf4j" % "jul-to-slf4j" % slf4jV,
  "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.11.0",
  "ch.qos.logback" % "logback-core" % logbackV,
  "ch.qos.logback" % "logback-classic" % logbackV,
  "net.logstash.logback" % "logstash-logback-encoder" % logstashEncV,
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % scalaLogSLF4JV,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLogV
)

val akka = Seq(
  akkaG %% "akka-actor" % akkaV,
  akkaG %% "akka-stream" % akkaV,
  akkaG %% "akka-slf4j" % akkaV,
  akkaG %% "akka-http" % akkaHttpV,
  akkaG %% "akka-cluster" % akkaV
)

val akkaCamel = Seq(
  "org.apache.camel" % "camel-core" % camelV,
  "org.apache.camel" % "camel-aws" % camelV,
  "org.apache.camel" % "camel-paho" % camelV,
  "org.apache.camel" % "camel-mqtt" % camelV,
  "com.typesafe.akka" %% "akka-camel" % akkaV exclude("org.apache.camel", "camel-core")
)

val jodaTime = "joda-time" % "joda-time" % "2.10"
val jodaConvert = "org.joda" % "joda-convert" % "2.1.1"
val joda = Seq(jodaTime, jodaConvert)

val json4sNative = json4sG %% "json4s-native" % json4sV
val json4sExt = json4sG %% "json4s-ext" % json4sV
val json4sJackson = "org.json4s" %% "json4s-jackson" % json4sV
val json4s = Seq(json4sNative, json4sExt, json4sJackson)

val spireMath = "org.spire-math" %% "spire" % spireV

// list of all available AWS artifacts: https://github.com/aws/aws-sdk-java/blob/master/aws-java-sdk-bom/pom.xml
val awsSqsSdk = Seq(
  awsG % "aws-java-sdk-sqs" % awsSdkV
)

val beeClient = "uk.co.bigbeeconsultants" %% "bee-client" % "0.29.1"

val msgpack4s = "org.velvia" %% "msgpack4s" % "0.6.0"
val msgpackScala = "org.msgpack" %% "msgpack-scala" % "0.6.11"
val guava = "com.google.guava" % "guava" % "26.0-jre"

val excludedLoggers = Seq(
  ExclusionRule(organization = "com.typesafe.scala-logging"),
  ExclusionRule(organization = "org.slf4j"),
  ExclusionRule(organization = "ch.qos.logback"),
  ExclusionRule(organization = "org.apache.logging")
)

val constructr = Seq(
  "de.heikoseeberger" %% "constructr" % "0.18.0",
  "de.heikoseeberger" %% "constructr-coordination-etcd" % "0.18.0"
)

val prometheus = Seq(
  "io.prometheus" % "simpleclient" % "0.3.0",
  "io.prometheus" % "simpleclient_hotspot" % "0.3.0",
  "io.prometheus" % "simpleclient_httpserver" % "0.3.0",
  "io.prometheus" % "simpleclient_pushgateway" % "0.3.0"
  , "com.workday" %% "prometheus-akka" % "0.8.5"
  , "org.aspectj" % "aspectjweaver" % "1.8.10"
)

val ubirchCamelUtils = ubirchUtilG %% "camel-utils" % "0.1.0" excludeAll (excludedLoggers: _*) // TODO migrate to 1.0.0
val ubirchConfig = ubirchUtilG %% "config" % "0.2.3" excludeAll (excludedLoggers: _*)
val ubirchCrypto = ubirchUtilG %% "crypto" % "0.4.11" excludeAll (excludedLoggers: _*)
val ubirchElasticsearchClientBinary = ubirchUtilG %% "elasticsearch-client-binary" % "3.0.1" excludeAll (excludedLoggers: _*)
val ubirchElasticsearchUtil = ubirchUtilG %% "elasticsearch-util" % "3.0.1" excludeAll (excludedLoggers: _*)
val ubirchJson = ubirchUtilG %% "json" % "0.5.1" excludeAll (excludedLoggers: _*)
val ubirchMongoTest = ubirchUtilG %% "mongo-test-utils" % "0.8.4" excludeAll (excludedLoggers: _*)
val ubirchMongo = ubirchUtilG %% "mongo-utils" % "0.8.4" excludeAll (excludedLoggers: _*)
val ubirchOidcUtils = ubirchUtilG %% "oidc-utils" % "0.8.3" excludeAll (excludedLoggers: _*)
val ubirchUtilRedisUtil = ubirchUtilG %% "redis-util" % "0.5.2"
val ubirchResponse = ubirchUtilG %% "response-util" % "0.4.1" excludeAll (excludedLoggers: _*)
val ubirchRestAkkaHttp = ubirchUtilG %% "rest-akka-http" % "0.4.0" excludeAll (excludedLoggers: _*)
val ubirchRestAkkaHttpTest = ubirchUtilG %% "rest-akka-http-test" % "0.4.0" excludeAll (excludedLoggers: _*)
val ubirchUUID = ubirchUtilG %% "uuid" % "0.1.3" excludeAll (excludedLoggers: _*)

val ubirchChainModel = "com.ubirch.chain" %% "model-rest" % "0.2.0" excludeAll (excludedLoggers: _*)

val ubirchNotary = "com.ubirch.notary" %% "client" % "0.3.3" excludeAll (
  excludedLoggers ++ Seq(ExclusionRule(organization = "com.ubirch.util", name = "json-auto-convert")): _*
  )
val ubirchUserClientRest = "com.ubirch.user" %% "client-rest" % "1.0.1" excludeAll (excludedLoggers: _*)
val ubirchKeyClientRest = "com.ubirch.key" %% "client-rest" % "0.11.1" excludeAll (excludedLoggers: _*)

/*
 * RESOLVER
 ********************************************************/

lazy val resolverSeebergerJson = Resolver.bintrayRepo("hseeberger", "maven")
lazy val resolverBeeClient = Resolver.bintrayRepo("rick-beton", "maven")
lazy val resolverEclipse = "eclipse-paho" at "https://repo.eclipse.org/content/repositories/paho-releases"
lazy val resolverElasticsearch = "elasticsearch-releases" at "https://artifacts.elastic.co/maven"
lazy val resolverTypesafeReleases = "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
lazy val resolverVelvia = "velvia maven" at "http://dl.bintray.com/velvia/maven"

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
