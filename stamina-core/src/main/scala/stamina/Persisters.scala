package stamina

import scala.reflect.ClassTag

/**
 * Persisters is the bridge between the type-safe world of <code>Persister[T, V]</code>
 * and the untyped, AnyRef world of Akka serializers. It wraps one or more
 * instances of <code>Persister[_, _]</code> and combines them together to form
 * one single entry-point for subclasses of <code>StaminaAkkaSerializer</code>
 *
 */
case class Persisters(persisters: List[Persister[_, _]]) {
  def canPersist(a: AnyRef): Boolean = persisters.exists(_.canPersist(a))
  def canUnpersist(p: Persisted): Boolean = persisters.exists(_.canUnpersist(p))

  // format: OFF
  def persist(anyref: AnyRef): Persisted = {
    persisters.find(_.canPersist(anyref))
              .map(_.persistAny(anyref))
              .getOrElse(throw UnregisteredTypeException(anyref))
  }

  def unpersist(persisted: Persisted): AnyRef = {
    persisters.find(_.canUnpersist(persisted))
              .map(_.unpersistAny(persisted))
              .getOrElse(throw UnsupportedDataException(persisted))
  }
  // format: ON

  def ++(other: Persisters): Persisters = Persisters(persisters ++ other.persisters)
}

object Persisters {
  def apply[T: ClassTag, V <: Version: VersionInfo](persister: Persister[T, V]): Persisters = apply(List(persister))
  def apply(first: Persister[_, _], rest: Persister[_, _]*): Persisters = apply(first :: rest.toList)
}
