// see http://www.scala-sbt.org/0.13/docs/Parallel-Execution.html for details
concurrentRestrictions in Global := Seq(
  Tags.limit(Tags.Test, 1)
)

val commonSettings = Seq(

  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
  scalaVersion := "2.13.8",
  scalacOptions ++= Seq("-feature"),
  organization := "com.ubirch.avatar",
  homepage := Some(url("http://ubirch.com")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/ubirch/ubirch-avatar-service"),
    "scm:git:git@github.com:ubirch/ubirch-avatar-service.git"
  )),
  (sys.env.get("CLOUDREPO_USER"), sys.env.get("CLOUDREPO_PW")) match {
    case (Some(username), Some(password)) =>
      println("USERNAME and/or PASSWORD found.")
      credentials += Credentials("ubirch.mycloudrepo.io", "ubirch.mycloudrepo.io", username, password)
    case _ =>
      println("USERNAME and/or PASSWORD is taken from /.sbt/.credentials.")
      credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
  },
  version := "0.6.6-SNAPSHOT",
  test in assembly := {},
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    resolverUbirchUtils,
    resolverUbirchMvn,
    ubirchMvnCentralProxy,
    ubirchRickBetonProxy
  ),
  publishMavenStyle := true,
  publishTo := Some("io.cloudrepo" at "https://ubirch.mycloudrepo.io/repositories/trackle-mvn")
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
      resolverVelvia,
      snapshotRepository
    ),
    mainClass in(Compile, run) := Some("com.ubirch.avatar.backend.Boot"),
    resourceGenerators in Compile += Def.task {
      generateDockerFile(baseDirectory.value / ".." / "Dockerfile.input", (assemblyOutputPath in assembly).value)
    }.taskValue
  )

lazy val cmdtools = project
  .settings(
    commonSettings,
    libraryDependencies += ubirchAvatarServiceClient
  )
  .dependsOn(core, util, testBase, config)
  .settings(
    description := "command line tools"
  )

lazy val core = project
  .settings(commonSettings)
  .dependsOn(config, modelDb, modelRest, util, testBase % "test")
  .settings(
    description := "business logic",
    libraryDependencies ++= depCore,
    resolvers ++= Seq(
      resolverEclipse,
      resolverElasticsearch
    )
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
  ubirchDeepCheckModel,
  ubirchJson,
  ubirchRestAkkaHttp,
  ubirchResponse,
  catsCore,
  scalatest % "test"

) ++ akka ++ prometheus ++ scalaLogging

lazy val depConfig = Seq(
  ubirchConfig
)

lazy val depCore = Seq(
  ubirchDeepCheckModel,
  ubirchElasticsearchUtils,
  ubirchCrypto,
  ubirchCryptoNew,
  ubirchMongo,
  ubirchResponse,
  ubirchUserClientRest,
  ubrichMsgPack,
  guava,
  scalatest % "test",
  akkaTestkit % "test"
) ++ akka ++ prometheus ++ scalaLogging

lazy val depClient = Seq() ++ scalaLogging

lazy val depClientRest = Seq(
  akkaHttp,
  akkaStream,
  akkaSlf4j,
  ubirchResponse,
  scalatest % "test"
) ++ scalaLogging

lazy val depAws = Seq(
  ubirchJson,
  ubirchUUID % "test",
  scalatest % "test"
) ++ scalaLogging

lazy val depModelDb = Seq(
  json4sNative,
  ubirchUUID
) ++ joda

lazy val depModelRest = Seq(
  json4sNative,
  ubirchUUID,
  ubirchAvatarServiceClient
) ++ joda

lazy val depUtil = Seq(
  ubirchCrypto,
  ubirchJson,
  ubirchElasticsearchUtils,
  ubirchMongo,
  ubirchUUID % "test",
  scalatest % "test"
) ++ json4s ++ scalaLogging

lazy val depTestBase = Seq(
  scalatest,
  ubirchMongo,
  ubirchRestAkkaHttp,
  ubirchUUID,
  ubirchCrypto
) ++ json4s ++ scalaLogging

/*
 * DEPENDENCIES
 ********************************************************/

// VERSIONS
val akkaV = "2.6.18"
val akkaHttpV = "10.2.7"
val akkaStreamKafkaV = "3.0.0"
val akkaStreamTestkitV = "3.0.0"
val json4sV = "4.0.5"
val catsV = "2.0.0"
val scalaTestV = "3.2.12"
val logbackV = "1.2.11"
val logstashEncV = "7.1.1"
val slf4jV = "1.7.36"
val log4jV = "2.17.2"
val scalaLogV = "3.9.4"
val scalaLogSLF4JV = "2.1.2"

// GROUP NAMES
val akkaG = "com.typesafe.akka"
val logbackG = "ch.qos.logback"
val json4sG = "org.json4s"
val ubirchUtilG = "com.ubirch.util"

val scalatest = "org.scalatest" %% "scalatest" % scalaTestV
val akkaHttpTestkit = akkaG %% "akka-http-testkit" % akkaHttpV
val akkaTestkit = akkaG %% "akka-testkit" % akkaV

val scalaLogging = Seq(
  "org.slf4j" % "slf4j-api" % slf4jV,
  "org.apache.logging.log4j" % "log4j-core" % log4jV,
  "ch.qos.logback" % "logback-classic" % logbackV,
  "net.logstash.logback" % "logstash-logback-encoder" % logstashEncV,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLogV
)

val akkaActor = akkaG %% "akka-actor" % akkaV
val akkaStream = akkaG %% "akka-stream" % akkaV
val akkaSlf4j = akkaG %% "akka-slf4j" % akkaV
val akkaHttp = akkaG %% "akka-http" % akkaHttpV
val akkaCluster = akkaG %% "akka-cluster" % akkaV
val akkaStreamKafka = akkaG %% "akka-stream-kafka" % akkaStreamKafkaV
val akkaTestKit = akkaG %% "akka-stream-kafka-testkit" % akkaStreamTestkitV % "test"

val akka = Seq(
  akkaActor,
  akkaStream,
  akkaSlf4j,
  akkaHttp,
  akkaCluster,
  akkaStreamKafka,
  akkaTestKit
)


// https://mvnrepository.com/artifact/org.typelevel/cats-core
val catsCore = "org.typelevel" %% "cats-core" % catsV

val jodaTime = "joda-time" % "joda-time" % "2.10.14"
val jodaConvert = "org.joda" % "joda-convert" % "2.2.2"
val joda = Seq(jodaTime, jodaConvert)

val json4sNative = json4sG %% "json4s-native" % json4sV
val json4sExt = json4sG %% "json4s-ext" % json4sV
val json4sJackson = "org.json4s" %% "json4s-jackson" % json4sV
val json4s = Seq(json4sNative, json4sExt, json4sJackson)


val msgpack4s = "org.velvia" %% "msgpack4s" % "0.6.0"
val ubrichMsgPack = "com.ubirch" % "ubirch-protocol-java" % "2.1.3-SNAPSHOT"
val guava = "com.google.guava" % "guava" % "26.0-jre"

val excludedLoggers = Seq(
  ExclusionRule(organization = "com.typesafe.scala-logging"),
  ExclusionRule(organization = "org.slf4j"),
  ExclusionRule(organization = "ch.qos.logback"),
  ExclusionRule(organization = "org.apache.logging")
)


val prometheus = Seq(
  "io.prometheus" % "simpleclient" % "0.14.1",
  "io.prometheus" % "simpleclient_hotspot" % "0.14.1",
  "io.prometheus" % "simpleclient_httpserver" % "0.14.1",
  "io.prometheus" % "simpleclient_pushgateway" % "0.14.1",
  "org.aspectj" % "aspectjweaver" % "1.9.9.1"
)

//Ubirch depedencies
val ubirchConfig = ubirchUtilG %% "ubirch-config-utils" % "0.2.5" excludeAll (excludedLoggers: _*)
val ubirchCrypto = ubirchUtilG %% "ubirch-crypto-utils" % "0.5.4" excludeAll (excludedLoggers: _*)
val ubirchCryptoNew = "com.ubirch" % "ubirch-crypto" % "2.1.3-SNAPSHOT" excludeAll (excludedLoggers: _*)
val ubirchDeepCheckModel = ubirchUtilG %% "ubirch-deep-check-utils" % "0.4.2" excludeAll (excludedLoggers: _*)
val ubirchElasticsearchUtils = ubirchUtilG %% "ubirch-elasticsearch-utils" % "0.2.9" excludeAll (excludedLoggers: _*)
val ubirchJson = ubirchUtilG %% "ubirch-json-utils" % "0.5.3" excludeAll (excludedLoggers: _*)
val ubirchMongo = ubirchUtilG %% "ubirch-mongo-utils" % "0.9.6" excludeAll (excludedLoggers: _*)
val ubirchResponse = ubirchUtilG %% "ubirch-response-utils" % "0.5.2" excludeAll (excludedLoggers: _*)
val ubirchRestAkkaHttp = ubirchUtilG %% "ubirch-rest-akka-http-utils" % "0.4.3" excludeAll (excludedLoggers: _*)
val ubirchUUID = ubirchUtilG %% "ubirch-uuid-utils" % "0.1.5" excludeAll (excludedLoggers: _*)

val ubirchAvatarServiceClient = "com.ubirch.avatar" %% "ubirch-avatar-service-client" % "0.7.0" excludeAll (excludedLoggers: _*)
val ubirchUserClientRest = "com.ubirch.user" %% "ubirch-user-service-client" % "1.0.7" excludeAll (excludedLoggers: _*)

/*
 * RESOLVER
 ********************************************************/

lazy val resolverSeebergerJson = Resolver.bintrayRepo("hseeberger", "maven")
lazy val resolverBeeClient = Resolver.bintrayRepo("rick-beton", "maven")
lazy val resolverEclipse = "eclipse-paho" at "https://repo.eclipse.org/content/repositories/paho-releases"
lazy val resolverElasticsearch = "elasticsearch-releases" at "https://artifacts.elastic.co/maven"
lazy val resolverTypesafeReleases = "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
lazy val resolverVelvia = "velvia maven" at "http://dl.bintray.com/velvia/maven"
lazy val resolverUbirchUtils = "ubirch.utils.cloudrepo" at "https://ubirch.mycloudrepo.io/repositories/ubirch-utils-mvn"
lazy val resolverUbirchMvn = "ubirch.mvn.cloudrepo" at "https://ubirch.mycloudrepo.io/repositories/mvn-public"
lazy val ubirchMvnCentralProxy = "mvn.central.proxy" at "https://ubirch.mycloudrepo.io/repositories/mvn-central-proxy"
lazy val ubirchRickBetonProxy = "mvn.rick.beton.proxy" at "https://ubirch.mycloudrepo.io/repositories/ubirch-mvn-rick-beton-proxy"
lazy val snapshotRepository = Resolver.sonatypeRepo("snapshots")


/*
 * MISC
 ********************************************************/

lazy val mergeStrategy = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList("org", "joda", "time", xs@_*) => MergeStrategy.first
    case m if m.toLowerCase.endsWith("application.conf") => MergeStrategy.concat
    case m if m.toLowerCase.endsWith("application.dev.conf") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("application.base.conf") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("application.docker.conf") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("logback.xml") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("logback-test.xml") => MergeStrategy.discard
    case m if m.toLowerCase.endsWith("logback.test.xml") => MergeStrategy.discard
    case m if m.toLowerCase.endsWith("logback.docker.xml") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("io.netty.versions.properties") => MergeStrategy.discard
    case m if m.toLowerCase.endsWith("module-info.class") => MergeStrategy.discard
    case m if m.endsWith("Logger.class") => MergeStrategy.first
    case m if m.endsWith("ServerKeys$.class") => MergeStrategy.first
    case m if m.endsWith("ServerKeys.class") => MergeStrategy.first
    case "reference.conf" => MergeStrategy.concat
    case n => MergeStrategy.defaultMergeStrategy(n)
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
