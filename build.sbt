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
  version := "0.3.0-SNAPSHOT",
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
  .aggregate(server, core, config, model, testBase)

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
    mainClass in(Compile, run) := Some("com.ubirch.avatar.backend.Boot"),
    resourceGenerators in Compile += Def.task {
      generateDockerFile(baseDirectory.value / ".." / "Dockerfile", name.value, version.value)
    }.taskValue
  )

lazy val core = project
  .settings(commonSettings: _*)
  .dependsOn(config, aws, transformer, model, testBase % "test")
  .settings(
    description := "business logic",
    libraryDependencies ++= depCore
  )

lazy val aws = project
  .settings(commonSettings: _*)
  .dependsOn(config, model, testBase % "test")
  .settings(
    description := "aws related stuff",
    libraryDependencies ++= depAws
  )

lazy val transformer = project
  .settings(commonSettings: _*)
  .dependsOn(config, model, testBase % "test")
  .settings(
    description := "device message transformation services",
    libraryDependencies ++= depTransform
  )

lazy val config = project
  .settings(commonSettings: _*)
  .settings(
    description := "config code",
    libraryDependencies += ubirchUtilConfig
  )

lazy val model = project
  .settings(commonSettings: _*)
  .settings(
    name := "model",
    description := "JSON models",
    libraryDependencies ++= depModel
  )

lazy val testBase = (project in file("test-base"))
  .settings(commonSettings: _*)
  .dependsOn(model, config)
  .settings(
    name := "test-base",
    description := "test tools",
    libraryDependencies ++= depTestBase,
    resolvers ++= Seq(
      resolverBeeClient
    )
  )

/*
 * MODULE DEPENDENCIES
 ********************************************************/

lazy val depServer = Seq(

  //akka
  akkaG %% "akka-actor" % akkaV,
  akkaG %% "akka-http-experimental" % akkaV,
  akkaG %% "akka-slf4j" % akkaV,

  //testing
  scalatest % "test",
  akkaG %% "akka-testkit" % akkaV % "test",
  akkaHttpTestkit % "test",

  ubirchUtilJson,
  ubirchUtilJsonAutoConvert,
  ubirchUtilRestAkkaHttp

) ++ scalaLogging

lazy val depCore = Seq(
  ubirchElasticsearchClientBinary,
  ubirchUtilUUID % "test",
  scalatest % "test"
) ++ scalaLogging

lazy val depTransform = Seq(
  ubirchUtilJson,
  akkaG %% "akka-actor" % akkaV,
  akkaG %% "akka-slf4j" % akkaV,
  akkaG %% "akka-testkit" % akkaV % "test",
  scalatest % "test"
) ++ akkaCamel ++ awsSqsSdk ++ scalaLogging

lazy val depAws = Seq(
  ubirchUtilJson,
  ubirchUtilUUID % "test",
  scalatest % "test"
) ++ awsIotSdk ++ scalaLogging

lazy val depModel = Seq(
  ubirchUtilJson,
  ubirchUtilJsonAutoConvert,
  ubirchUtilUUID
) ++ joda ++ json4s ++ scalaLogging

lazy val depTestBase = Seq(
  scalatest,
  akkaHttpTestkit,
  beeClient,
  ubirchUtilUUID
)

/*
 * DEPENDENCIES
 ********************************************************/

// VERSIONS
lazy val akkaV = "2.4.11"
lazy val json4sV = "3.4.2"
lazy val awsSdkV = "1.10.77"
lazy val scalaTestV = "3.0.0"
lazy val camelV = "2.18.0"

// GROUP NAMES
lazy val akkaG = "com.typesafe.akka"
lazy val logbackG = "ch.qos.logback"
lazy val json4sG = "org.json4s"
lazy val awsG = "com.amazonaws"
lazy val ubirchUtilG = "com.ubirch.util"

lazy val scalatest = "org.scalatest" %% "scalatest" % scalaTestV
lazy val akkaHttpTestkit = akkaG %% "akka-http-testkit" % akkaV

lazy val scalaLogging = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0" exclude("org.slf4j", "slf4j-api"),
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "ch.qos.logback" % "logback-core" % "1.1.7"
)

lazy val akkaCamel = Seq(
  "org.apache.camel" % "camel-core" % camelV,
  "com.typesafe.akka" %% "akka-camel" % akkaV
)

lazy val joda = Seq(jodaTime, jodaConvert)
lazy val jodaTime = "joda-time" % "joda-time" % "2.9.4"
lazy val jodaConvert = "org.joda" % "joda-convert" % "1.8"

lazy val json4s = Seq(json4sNative, json4sExt, json4sJackson)
lazy val json4sNative = json4sG %% "json4s-native" % json4sV
lazy val json4sExt = json4sG %% "json4s-ext" % json4sV
lazy val json4sJackson = "org.json4s" %% "json4s-jackson" % json4sV

// seed for all available AWS artifacts: https://github.com/aws/aws-sdk-java/blob/master/aws-java-sdk-bom/pom.xml

lazy val awsIotSdk = Seq(awsG % "aws-java-sdk-iot" % awsSdkV)
lazy val awsSqsSdk = Seq(awsG % "aws-java-sdk-sqs" % awsSdkV)
//lazy val awsSdk = Seq(awsG % "aws-java-sdk" % awsSdkV)

lazy val beeClient = "uk.co.bigbeeconsultants" %% "bee-client" % "0.29.1"

lazy val ubirchUtilConfig = ubirchUtilG %% "config" % "0.1"
lazy val ubirchElasticsearchClientBinary = ubirchUtilG %% "elasticsearch-client-binary" % "0.2.4"
lazy val ubirchUtilJson = ubirchUtilG %% "json" % "0.2"
lazy val ubirchUtilJsonAutoConvert = ubirchUtilG %% "json-auto-convert" % "0.3"
lazy val ubirchUtilRestAkkaHttp = ubirchUtilG %% "rest-akka-http" % "0.3"
lazy val ubirchUtilUUID = ubirchUtilG %% "uuid" % "0.1"

/*
 * RESOLVER
 ********************************************************/

lazy val resolverSeebergerJson = Resolver.bintrayRepo("hseeberger", "maven")
lazy val resolverBeeClient = Resolver.bintrayRepo("rick-beton", "maven")

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

def generateDockerFile(file: File, nameString: String, versionString: String): Seq[File] = {

  //  val jar = "avatarService-%s-assembly-%s.jar".format(nameString, versionString)
  //assembleArtifact.
  val jar = "./server/target/scala-2.11/server-assembly-0.3.0-SNAPSHOT.jar"
  val contents =
    s"""FROM java
        |ADD $jar /app/$jar
        |ENTRYPOINT ["java", "-jar", "$jar"]
        |""".stripMargin
  IO.write(file, contents)
  Seq(file)

}
