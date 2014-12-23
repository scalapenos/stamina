import sbt._
import sbt.Keys._

object Dependencies {
  val akkaActor     = "com.typesafe.akka"  %% "akka-actor"       % "2.3.8"
  val scalaReflect  = "org.scala-lang"     %  "scala-reflect"    % "2.11.4"

  // Required for the json implementation
  val sprayJson     = "io.spray"           %% "spray-json"       % "1.3.1"
  val jsonLenses    = "net.virtual-void"   %%  "json-lenses"     % "0.6.0"

  val scalatest     = "org.scalatest"      %% "scalatest"        % "2.2.3"

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
}
