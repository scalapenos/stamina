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
  lazy val currentManifest = Manifest(key, currentVersion)

  def persist(t: T): Array[Byte]
  def unpersist(manifest: Manifest, persisted: Array[Byte]): T

  def canPersist(a: AnyRef): Boolean = convertToT(a).isDefined
  def canUnpersist(m: Manifest): Boolean = m.key == key && m.version <= currentVersion

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

  private[stamina] def unpersistAny(manifest: Manifest, persistedBytes: Array[Byte]): AnyRef = {
    Try(unpersist(manifest, persistedBytes).asInstanceOf[AnyRef]) match {
      case Success(anyref) ⇒ anyref
      case Failure(error)  ⇒ throw UnrecoverableDataException(manifest, error)
    }
  }
}
