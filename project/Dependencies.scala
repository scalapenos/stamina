import sbt._
import sbt.Keys._

object Dependencies {
  // format: OFF
  val akkaActor  = "com.typesafe.akka" %% "akka-actor"  % "2.5.25"
  val sprayJson  = "io.spray"          %% "spray-json"  % "1.3.5"
  val jsonLenses = "net.virtual-void"  %% "json-lenses" % "0.6.2"
  val scalatest  = "org.scalatest"     %% "scalatest"   % "3.1.0"
  // format: ON

  // Dependency scoping functions
  def compileDeps(deps: ModuleID*): Seq[ModuleID] = deps map (_ % Compile)
  def testDeps(deps: ModuleID*): Seq[ModuleID] = deps map (_ % Test)
  def providedDeps(deps: ModuleID*): Seq[ModuleID] = deps map (_ % Provided)
}
