import sbt._
import sbt.Keys._

object Build extends Build {
  import Dependencies._
  import Formatting._

  lazy val basicSettings = Seq(
    organization := "com.scalapenos",
    version := "0.1.1-SNAPSHOT",
    scalaVersion := "2.11.7",
    crossScalaVersions := Seq("2.11.7", "2.10.5"),
    crossVersion := CrossVersion.binary,
    incOptions := incOptions.value.withNameHashing(true),
    scalacOptions := Seq(
      "-encoding", "utf8",
      "-target:jvm-1.7",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-Xlint",
      "-Xlog-reflective-calls"
    ) ++ versionSpecificScalacOptions(scalaVersion.value)
  )

  lazy val publishingSettings = Seq(
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    credentials += Credentials(
      "Sonatype Nexus Repository Manager",
      "oss.sonatype.org",
      sys.env("SONATYPE_USERNAME"),
      sys.env("SONATYPE_PASSWORD"))
  )

  lazy val libSettings = basicSettings ++ formattingSettings ++ publishingSettings

  lazy val root = Project("stamina", file("."))
    .settings(basicSettings: _*)
    .settings(publishingSettings: _*)
    .aggregate(
      core,
      json,
      testkit
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
      compile(
        scalatest,
        base64
      )
    )

  private def versionSpecificScalacOptions(versionOfScala: String): Seq[String] = {
    CrossVersion.partialVersion(versionOfScala) match {
      case Some((major, minor)) if major >= 2 && minor >= 11 => Seq("-Ywarn-unused", "-Ywarn-unused-import")
      case _ => Nil
    }
  }
}
