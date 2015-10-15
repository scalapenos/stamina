import sbt._
import sbt.Keys._

object Dependencies {
  val akkaActor     = "com.typesafe.akka"  %% "akka-actor"       % "2.4.0"
  val sprayJson     = "io.spray"           %% "spray-json"       % "1.3.2"
  val jsonLenses    = "net.virtual-void"   %% "json-lenses"      % "0.6.1"
  val scalatest     = "org.scalatest"      %% "scalatest"        % "2.2.5"
  val base64        = "me.lessis"          %% "base64"           % "0.2.0"

  // Dependency on the version of scala-reflect linked to the cross-build scala version
  def scalaReflect(versionOfScala: String) = "org.scala-lang" % "scala-reflect" % versionOfScala

  def quasiQuotes(versionOfScala: String) = {
    CrossVersion.partialVersion(versionOfScala) match {
      case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq.empty
      case Some((2, 10)) => Seq(
        compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
        "org.scalamacros" %% "quasiquotes" % "2.0.1" cross CrossVersion.binary
      )
    }
  }

  // Dependency scoping functions
  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
}
