import sbt._
import sbt.Keys._

object Dependencies {
  val akkaActor     = "com.typesafe.akka"  %% "akka-actor"       % "2.3.9"
  val sprayJson     = "io.spray"           %% "spray-json"       % "1.3.1"
  val jsonLenses    = "net.virtual-void"   %% "json-lenses"      % "0.6.0"
  val scalatest     = "org.scalatest"      %% "scalatest"        % "2.2.4"

  // Dependency on the version of scala-reflect linked to the cross-build scala version
  def scalaReflect(versionOfScala: String) = "org.scala-lang" % "scala-reflect" % versionOfScala

  // Dependency scoping functions
  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
}
