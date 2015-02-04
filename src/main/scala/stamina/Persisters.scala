package stamina

import scala.reflect.ClassTag

/**
 *
 */
case class Persisters(persisters: List[Persister[_, _]]) {
  def canPersist(a: AnyRef): Boolean = persisters.exists(_.canPersist(a))
  def canUnpersist(p: Persisted): Boolean = persisters.exists(_.canUnpersist(p))

  def persist(a: AnyRef): Persisted = persisters.find(_.canPersist(a)).map(_.persistAny(a)).getOrElse(throw new IllegalArgumentException("Persisters no persister found"))
  def unpersist(persisted: Persisted): AnyRef = persisters.find(_.canUnpersist(persisted)).map(_.unpersistAny(persisted)).getOrElse(throw new IllegalArgumentException("Persisters no unpersister found"))

  def ++(other: Persisters): Persisters = Persisters(persisters ++ other.persisters)
}

object Persisters {
  def apply[T: ClassTag, V <: Version: VersionInfo](persister: Persister[T, V]): Persisters = apply(List(persister))
  def apply(first: Persister[_, _], rest: Persister[_, _]*): Persisters = apply(first :: rest.toList)
}
