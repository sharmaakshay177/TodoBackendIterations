
val ScalatraVersion = "2.7.1"
val ScalaLoggingVersion = "3.9.4"
val Json4sVersion = "4.0.2"
val circeVersion = "0.14.1"

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / organization := "com.github.sharmaakshay177"

lazy val hello = (project in file("."))
  .settings(
    name := "TODOBackendIterations",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.scalatra" %% "scalatra" % ScalatraVersion,
      "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
      "org.scalatra" %% "scalatra-json" % ScalatraVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.json4s" %% "json4s-jackson" % Json4sVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.6" % "runtime",
      "org.eclipse.jetty" % "jetty-webapp" % "11.0.6" % "container",
      "javax.servlet" % "javax.servlet-api" % "4.0.1" % "provided",
      "org.scalamock" %% "scalamock" % "5.1.0" % Test,
      "org.scalatest" %% "scalatest" % "3.2.9" % Test
    ),
  )

enablePlugins(SbtTwirl)
enablePlugins(JettyPlugin)
