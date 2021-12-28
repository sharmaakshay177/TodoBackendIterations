val scalaVer = "2.13.6"

val attoVersion     = "0.9.5"
val zioVersion      = "2.0.0-RC1"
val zioMagicVersion = "0.3.11"

lazy val compileDependencies = Seq(
  "dev.zio"              %% "zio"       % zioVersion,
  "io.github.kitlangton" %% "zio-magic" % zioMagicVersion,
  "org.tpolecat"         %% "atto-core" % attoVersion
) map (_ % Compile)

lazy val testDependencies = Seq(
  "dev.zio" %% "zio-test"     % zioVersion,
  "dev.zio" %% "zio-test-sbt" % zioVersion
) map (_ % Test)

lazy val settings = Seq(
  name := "ZioConcepts",
  version := "0.1.0-SNAPSHOT-TTT",
  scalaVersion := scalaVer,
  scalacOptions += "-Ymacro-annotations",
  libraryDependencies ++= compileDependencies ++ testDependencies,
  testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
)

lazy val root = (project in file("ZioConcepts"))
  .settings(settings)