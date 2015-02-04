package stamina

import scala.reflect.ClassTag

/**
 * A Persister[T, V] provides a type-safe API for persisting instances of T
 * at version V and unpersisting persisted instances of T for all versions up
 * to and including version V.
 */
abstract class Persister[T: ClassTag, V <: Version: VersionInfo](val key: String) {
  protected lazy val version = Version.numberFor[V]

  def persist(t: T): Persisted
  def unpersist(persisted: Persisted): T

  def canPersist(a: AnyRef): Boolean = convertToT(a).isDefined
  def canUnpersist(p: Persisted): Boolean = p.key == key && p.version == version

  private[stamina] def convertToT(any: AnyRef): Option[T] = any match {
    case t: T ⇒ Some(t)
    case _    ⇒ None
  }

  private[stamina] def persistAny(any: AnyRef): Persisted = {
    convertToT(any).map(persist(_)).getOrElse(
      throw new IllegalArgumentException(
        s"persistAny() was called on Persister[${implicitly[ClassTag[T]].runtimeClass}] with an instance of ${any.getClass}."
      )
    )
  }

  private[stamina] def unpersistAny(persisted: Persisted): AnyRef = unpersist(persisted).asInstanceOf[AnyRef]
}
