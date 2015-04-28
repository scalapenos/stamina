package stamina
package testkit

import scala.util._
import scala.annotation.tailrec

trait StaminaTestKit { self: org.scalatest.WordSpecLike ⇒

  val defaultSampleId = "default"
  case class PersistableSample(sampleId: String, persistable: AnyRef, description: Option[String]) {
    override def toString = persistable.getClass.getSimpleName + description.map(" " + _).getOrElse("")
  }

  def sample(persistable: AnyRef) = new PersistableSample(defaultSampleId, persistable, None)
  def sample(sampleId: String, persistable: AnyRef) = new PersistableSample(sampleId, persistable, Some(sampleId))
  def sample(sampleId: String, persistable: AnyRef, description: String) = new PersistableSample(sampleId, persistable, Some(description))

  implicit class TestablePersisters(persisters: Persisters) extends org.scalatest.Matchers {
    def generateTestsFor(samples: PersistableSample*): Unit = {
      samples.foreach { sample ⇒
        performRoundtrip(sample)
        deserializeStoredVersions(sample)
      }
      // Here we could verify test coverage
    }

    private def performRoundtrip(sample: PersistableSample) = {
      s"persist and unpersist $sample" in {
        persisters.canPersist(sample.persistable) should be(true)
        persisters.unpersist(persisters.persist(sample.persistable)) should equal(sample.persistable)
      }
    }

    @tailrec
    private def deserializeStoredVersions(sample: PersistableSample, fromVersion: Int = 1): Unit = {

      s"deserialize the stored serialized form of $sample version $fromVersion" in {
        verifyByteStringDeserialization(sample, fromVersion)
      }

      if (fromVersion < latestVersion(sample.persistable))
        deserializeStoredVersions(sample, fromVersion + 1)
    }

    def latestVersion(persistable: AnyRef) = persisters.persisters.find(_.canPersist(persistable)).map(_.currentVersion).max

    private def verifyByteStringDeserialization(sample: PersistableSample, version: Int): Unit = {
      val serialized = persisters.persist(sample.persistable)
      byteStringFromFile(serialized.key, version, sample.sampleId) match {
        case Success(binary) ⇒
          persisters.unpersist(binary) should be(sample.persistable)
        case Failure(x: java.io.FileNotFoundException) if version < latestVersion(sample.persistable) ⇒
          ()
        case Failure(other) ⇒
          val writtenToPath = saveByteArrayToTargetSerializationDirectory(serialized.bytes.toArray, serialized.key, version, sample.sampleId)
          fail(s"The file /src/test/resources/serialization/${filename(serialized.key, version, sample.sampleId)} for $sample is missing in the /src/test/resources/serialization serialization resource directory.\n" +
            s"Serialization file written to: $writtenToPath. Please copy this file to: /src/test/resources/serialization/")
      }
    }

    private def byteStringFromFile(key: String, version: Int, sampleId: String) = {
      import scala.io.Source
      val resourceName = s"/serialization/${filename(key, version, sampleId)}"
      val isOpt = Option(this.getClass.getResourceAsStream(resourceName))
      isOpt
        .map(is ⇒ Try(Persisted(key, version, akka.util.ByteString(base64.Decode(Source.fromInputStream(is).mkString).right.get))))
        .getOrElse(Failure(new java.io.FileNotFoundException(resourceName)))
    }

    private def saveByteArrayToTargetSerializationDirectory(bytes: Array[Byte], key: String, version: Int, sampleId: String) = {
      import java.nio.file._
      val path = Paths.get(targetSerializationDirectory, filename(key, version, sampleId))
      Files.write(path, base64.Encode(bytes), StandardOpenOption.CREATE)
      path.toAbsolutePath
    }

    private def filename(key: String, version: Int, sampleId: String) = s"$key-v$version-" + sampleId.replaceAll("\\s+", "-")

    def targetSerializationDirectory = tempTargetSerializationDirectory
    val projectTargetSerializationDirectory = s"$projectPath/src/test/resources/serialization"
    val tempTargetSerializationDirectory = System.getProperty("java.io.tmpdir")
    private def projectPath = currentPath.substring(0, currentPath.indexOf("/target"))
    private def currentPath = getClass.getResource("").getPath
  }
}
