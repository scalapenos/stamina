import sbt._
import sbt.Keys._

object Dependencies {
  // format: OFF
  val akkaActor  = "com.typesafe.akka" %% "akka-actor"  % "2.5.8"
  val sprayJson  = "io.spray"          %% "spray-json"  % "1.3.4"
  val jsonLenses = "net.virtual-void"  %% "json-lenses" % "0.6.2"
  val scalatest  = "org.scalatest"     %% "scalatest"   % "3.0.4"
  // format: ON

  // Only used by the tests
  val sprayJsonShapeless = "com.github.fommil" %% "spray-json-shapeless" % "1.3.0"

  // Dependency scoping functions
  def compileDeps(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def testDeps(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def providedDeps(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
}
