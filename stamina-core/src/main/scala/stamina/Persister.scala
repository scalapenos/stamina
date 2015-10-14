package stamina

import scala.reflect.ClassTag
import scala.util._

/**
 * A Persister[T, V] provides a type-safe API for persisting instances of T
 * at version V and unpersisting persisted instances of T for all versions up
 * to and including version V.
 */
abstract class Persister[T: ClassTag, V <: Version: VersionInfo](val key: String) {
  lazy val currentVersion = Version.numberFor[V]

  def persist(t: T): Array[Byte]
  def unpersist(persisted: Persisted): T
  def unpersist(manifest: String, persisted: Array[Byte]): T =
    // TODO I should really remove that one but this is easier for the PoC
    unpersist(Persisted(Manifest.key(manifest), Manifest.version(manifest), persisted))

  def canPersist(a: AnyRef): Boolean = convertToT(a).isDefined
  lazy val currentManifest = Manifest.encode(key, currentVersion)
  /* To be overridden when a Persister can persist multiple versions */
  def canUnpersist(p: Persisted): Boolean = canUnpersist(Manifest.encode(p.key, p.version))
  def canUnpersist(m: String): Boolean = Manifest.key(m) == key && Manifest.version(m) == currentVersion

  private[stamina] def convertToT(any: AnyRef): Option[T] = any match {
    case t: T ⇒ Some(t)
    case _    ⇒ None
  }

  private[stamina] def persistAny(any: AnyRef): Array[Byte] = {
    convertToT(any).map(persist(_)).getOrElse(
      throw new IllegalArgumentException(
        s"persistAny() was called on Persister[${implicitly[ClassTag[T]].runtimeClass}] with an instance of ${any.getClass}."
      )
    )
  }

  private[stamina] def unpersistAny(manifest: String, persistedBytes: Array[Byte]): AnyRef = {
    Try(unpersist(manifest, persistedBytes).asInstanceOf[AnyRef]) match {
      case Success(anyref) ⇒ anyref
      case Failure(error) ⇒
        // TODO simplify
        val persisted = Persisted(key, Manifest.version(manifest), ByteString(persistedBytes))
        throw UnrecoverableDataException(persisted, error)
    }
  }
}
