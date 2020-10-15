// see http://www.scala-sbt.org/0.13/docs/Parallel-Execution.html for details
concurrentRestrictions in Global := Seq(
  Tags.limit(Tags.Test, 1)
)

val commonSettings = Seq(

  publishConfiguration := publishConfiguration.value.withOverwrite(true),
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
  scalaVersion := "2.11.12",
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
    resolverUbirchUtils
  ),
  publishMavenStyle := true,
  publishTo := Some("io.cloudrepo" at "https://ubirch.mycloudrepo.io/repositories/trackle-mvn")

  //  https://www.scala-lang.org/2019/10/17/dependency-management.html
  //  , conflictManager := ConflictManager.strict
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
  ubirchDeepCheckModel,
  ubirchJson,
  ubirchRestAkkaHttp,
  ubirchResponse,
  ubirchOidcUtils,

  //testing
  scalatest % "test"

) ++ akka ++ akkaCamel ++ prometheus ++ constructr ++ scalaLogging

lazy val depConfig = Seq(
  ubirchCamelUtils,
  ubirchConfig
)

lazy val depCore = Seq(
  ubirchDeepCheckModel,
  ubirchElasticsearchUtils,
  ubirchCamelUtils,
  ubirchCrypto,
  ubirchMongo,
  ubirchResponse,
  ubirchIdServiceClient,
  ubirchUserClientRest,
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
) ++ awsSqsSdk ++ scalaLogging

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
  ubirchOidcUtils,
  ubirchUUID % "test",
  scalatest % "test"
) ++ json4s ++ scalaLogging

lazy val depTestBase = Seq(
  scalatest,
  ubirchMongo,
  ubirchRestAkkaHttp,
  beeClient,
  ubirchUUID,
  ubirchCrypto
) ++ json4s ++ scalaLogging

/*
 * DEPENDENCIES
 ********************************************************/

// VERSIONS
val akkaV = "2.5.21"
val akkaHttpV = "10.1.3"
val json4sV = "3.6.0"
val awsSdkV = "1.11.438"
val camelV = "2.23.1"
val scalaTestV = "3.0.5"
val spireV = "0.13.0"
val logbackV = "1.2.3"
val logstashEncV = "5.0"
val slf4jV = "1.7.25"
val log4jV = "2.13.0"
val scalaLogV = "3.9.0"
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
  //  "org.slf4j" % "log4j-over-slf4j" % slf4jV,
  //  "org.slf4j" % "jul-to-slf4j" % slf4jV,
  //  "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.11.0",
  //  "ch.qos.logback" % "logback-core" % logbackV,
  "ch.qos.logback" % "logback-classic" % logbackV,
  "net.logstash.logback" % "logstash-logback-encoder" % logstashEncV,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLogV
)

val akkaActor = akkaG %% "akka-actor" % akkaV
val akkaStream = akkaG %% "akka-stream" % akkaV
val akkaSlf4j = akkaG %% "akka-slf4j" % akkaV
val akkaHttp = akkaG %% "akka-http" % akkaHttpV
val akkaCluster = akkaG %% "akka-cluster" % akkaV
val akka = Seq(
  akkaActor,
  akkaStream,
  akkaSlf4j,
  akkaHttp,
  akkaCluster
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
  "io.prometheus" % "simpleclient_pushgateway" % "0.3.0",
  "com.workday" %% "prometheus-akka" % "0.8.5",
  "org.aspectj" % "aspectjweaver" % "1.8.10"
)

//Ubirch depedencies
val ubirchCamelUtils = ubirchUtilG %% "ubirch-camel-utils" % "0.1.1" excludeAll (excludedLoggers: _*) // TODO migrate to 1.0.0
val ubirchConfig = ubirchUtilG %% "ubirch-config-utils" % "0.2.4" excludeAll (excludedLoggers: _*)
val ubirchCrypto = ubirchUtilG %% "ubirch-crypto-utils" % "0.5.3" excludeAll (excludedLoggers: _*)
val ubirchDeepCheckModel = ubirchUtilG %% "ubirch-deep-check-utils" % "0.4.1" excludeAll (excludedLoggers: _*)
val ubirchElasticsearchUtils = ubirchUtilG %% "ubirch-elasticsearch-utils" % "0.2.5" excludeAll (excludedLoggers: _*)
val ubirchJson = ubirchUtilG %% "ubirch-json-utils" % "0.5.2" excludeAll (excludedLoggers: _*)
val ubirchMongo = ubirchUtilG %% "ubirch-mongo-utils" % "0.9.5" excludeAll (excludedLoggers: _*)
val ubirchOidcUtils = ubirchUtilG %% "ubirch-oidc-utils" % "0.8.15" excludeAll (excludedLoggers: _*)
val ubirchUtilRedisUtil = ubirchUtilG %% "ubirch-redis-utils" % "0.6.1"
val ubirchResponse = ubirchUtilG %% "ubirch-response-utils" % "0.5.1" excludeAll (excludedLoggers: _*)
val ubirchRestAkkaHttp = ubirchUtilG %% "ubirch-rest-akka-http-utils" % "0.4.1" excludeAll (excludedLoggers: _*)
val ubirchUUID = ubirchUtilG %% "ubirch-uuid-utils" % "0.1.4" excludeAll (excludedLoggers: _*)

val ubirchAvatarServiceClient = "com.ubirch.avatar" %% "ubirch-avatar-service-client" % "0.6.6" excludeAll (excludedLoggers: _*)
val ubirchIdServiceClient = "com.ubirch.id" %% "ubirch-id-service-client" % "0.6.6" excludeAll (excludedLoggers: _*)
val ubirchUserClientRest = "com.ubirch.user" %% "ubirch-user-service-client" % "1.0.5" excludeAll (excludedLoggers: _*)

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

/*
 * MISC
 ********************************************************/

lazy val mergeStrategy = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList("org", "joda", "time", xs@_*) => MergeStrategy.first
    //case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    //case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
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
