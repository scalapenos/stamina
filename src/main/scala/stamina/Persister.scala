package stamina

import scala.reflect.ClassTag

/**
 * A Persister[T, V] provides a type-safe API for persisting instances of T
 * at version V and unpersisting persisted instances of T for all versions up
 * to and including version V.
 */
abstract class Persister[T: ClassTag, V <: Version: VersionInfo](val key: String) {
  protected lazy val version = Version.numberFor[V]

  def canPersist(a: AnyRef): Boolean = toT(a).isDefined

  def persist(t: T): Persisted
  def canUnpersist(p: Persisted): Boolean
  def unpersist(persisted: Persisted): T

  private[stamina] def toT(any: AnyRef): Option[T] = any match {
    case t: T ⇒ Some(t)
    case _    ⇒ None
  }

  private[stamina] def persistAny(any: AnyRef): Persisted = toT(any).map(persist).getOrElse(throw new IllegalArgumentException(s"Persister"))
  private[stamina] def unpersistAny(persisted: Persisted): AnyRef = unpersist(persisted).asInstanceOf[AnyRef]
}
