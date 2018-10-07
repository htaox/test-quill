name := "test-quill"
organization in ThisBuild := "com.eztier"
scalaVersion in ThisBuild := "2.12.4"

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8",
  "-Ylog-classpath"
)

lazy val global = project
  .in(file("."))
  .settings(settings)
  .aggregate(
    common,
    database,
    http
  )
  
lazy val commonSettings = Seq(
  version := "0.1.1",
  organization := "com.eztier",
  scalaVersion := "2.12.4",
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    Resolver.sonatypeRepo("public"),
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val settings = commonSettings

lazy val common = project
  .settings(
    name := "common",
    settings,
    libraryDependencies ++= Seq(
      scalaTest,
      logback
    )
  )

val akka = "com.typesafe.akka"
val akkaHttpV = "10.1.5"

val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

val akkaStream = akka %% "akka-stream" % "2.5.9"
val akkaSlf4j = akka %% "akka-slf4j" % "2.5.9"
val akkaStreamTestkit = akka %% "akka-stream-testkit" % "2.5.9" % Test
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

// HTTP server
val akkaHttp = akka %% "akka-http" % akkaHttpV
val akkaHttpCore = akka %% "akka-http-core" % akkaHttpV
val akkaHttpSprayJson = akka %% "akka-http-spray-json" % akkaHttpV
val akkaHttpTestkit = akka %% "akka-http-testkit" % akkaHttpV % Test

// Support of CORS requests, version depends on akka-http
// val akkaHttpCors = "ch.megard" %% "akka-http-cors" % "0.3.0"

// PostgreSQL
val quillAsyncPostgres = "io.getquill" %% "quill-async-postgres" % "2.5.5-SNAPSHOT"

lazy val database = project.
  settings(
    name := "database",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      scalaTest,
      logback,
      akkaStream,
      akkaSlf4j,
      akkaStreamTestkit,
      quillAsyncPostgres
    )
  ).dependsOn(
    common
  )
  
lazy val http = project.
  settings(
    name := "http",
    settings,
    assemblySettings,
    libraryDependencies ++= Seq(
      scalaTest,
      logback,
      akkaStream,
      akkaSlf4j,
      akkaStreamTestkit,
      akkaHttp,
      akkaHttpCore,
      akkaHttpSprayJson,
      akkaHttpTestkit
    )
  ).dependsOn(
    common
  )
  
lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _                             => MergeStrategy.first
  }
)
