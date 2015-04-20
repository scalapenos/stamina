import sbt._
import sbt.Keys._

object Build extends Build {
  import Dependencies._
  import Formatting._

  lazy val basicSettings = Seq(
    organization := "com.scalapenos",
    version := "0.1.0",
    scalaVersion := "2.11.6",
    crossScalaVersions := Seq("2.11.6", "2.10.5"),
    crossVersion := CrossVersion.binary,
    scalacOptions := basicScalacOptions,
    incOptions := incOptions.value.withNameHashing(true)
  )

  lazy val libSettings = basicSettings ++ formattingSettings

  lazy val root = Project("stamina", file("."))
    .settings(basicSettings: _*)
    .aggregate(
      core,
      json
    )

  lazy val core = Project("stamina-core", file("stamina-core"))
    .settings(libSettings: _*)
    .settings(libraryDependencies ++=
      compile(
        akkaActor,
        scalaReflect(scalaVersion.value)
      ) ++
      quasiQuotes(scalaVersion.value) ++
      test(
        scalatest
      )
    )

  lazy val json = Project("stamina-json", file("stamina-json"))
    .dependsOn(core)
    .settings(libSettings: _*)
    .settings(libraryDependencies ++=
      compile(
        sprayJson,
        jsonLenses
      ) ++
      quasiQuotes(scalaVersion.value) ++
      test(
        scalatest
      )
    )

  lazy val testkit = Project("stamina-testkit", file("stamina-testkit"))
    .dependsOn(core)
    .settings(libSettings: _*)
    .settings(libraryDependencies ++=
      test(
        scalatest
      )
    )

  val basicScalacOptions = Seq(
    "-encoding", "utf8",
    "-target:jvm-1.7",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-Xlint",
    "-Xlog-reflective-calls",
        "-Ywarn-unused",
    "-Ywarn-unused-import"
  )
}
