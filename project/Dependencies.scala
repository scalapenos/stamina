import sbt._
import sbt.Keys._

object Dependencies {
  val akkaActor     = "com.typesafe.akka"  %% "akka-actor"       % "2.4.11"
  val sprayJson     = "io.spray"           %% "spray-json"       % "1.3.2"
  val jsonLenses    = "net.virtual-void"   %% "json-lenses"      % "0.6.1"
  val scalatest     = "org.scalatest"      %% "scalatest"        % "3.0.0"
  val base64        = "me.lessis"          %% "base64"           % "0.2.0"

  // Only used by the tests
  val sprayJsonShapeless = "com.github.fommil" %% "spray-json-shapeless" % "1.1.0"

  // Dependency scoping functions
  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
}
