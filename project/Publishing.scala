import sbt._
import sbt.Keys._
import xerial.sbt.Sonatype._
import SonatypeKeys._

object Publishing {
  lazy val publishingSettings = Seq(
    homepage := Some(url("https://github.com/scalapenos/stamina")),
    pomExtra := (
      <developers>
        <developer>
          <id>agemooij</id>
          <name>Age Mooij</name>
          <url>http://github.com/agemooij</url>
        </developer>
        <developer>
          <id>raboof</id>
          <name>Arnout Engelen</name>
          <url>http://github.com/raboof</url>
        </developer>
        <developer>
          <id>larochef</id>
          <name>François LAROCHE</name>
          <url>http://github.com/larochef</url>
        </developer>
      </developers>))
}
