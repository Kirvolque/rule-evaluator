ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "RuleEvaluator",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "org.typelevel" %% "cats-core" % "2.9.0",
      "com.github.scopt" %% "scopt" % "4.1.0",
      "dev.zio" %% "zio" % "2.0.10",
      "dev.zio" %% "zio-streams" % "2.0.10",
      "dev.zio" %% "zio-test" % "2.0.10" % "test",
    )
  )
