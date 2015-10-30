package stamina

import scala.reflect.ClassTag

/**
 * Persisters is the bridge between the type-safe world of <code>Persister[T, V]</code>
 * and the untyped, AnyRef world of Akka serializers. It wraps one or more
 * instances of <code>Persister[_, _]</code> and combines them together to form
 * one single entry-point for subclasses of <code>StaminaAkkaSerializer</code>
 *
 */
case class Persisters[P <: AnyRef](persisters: List[Persister[_, P, _]]) {
  def canPersist(a: AnyRef): Boolean = persisters.exists(_.canPersist(a))
  def canUnpersist(manifest: Manifest): Boolean = persisters.exists(_.canUnpersist(manifest))

  // format: OFF
  private def persister[T <: AnyRef](anyref: T): Persister[T, P, _] =
    persisters
      .find(_.canPersist(anyref))
      .map(_.asInstanceOf[Persister[T, P, _]])
      .getOrElse(throw UnregisteredTypeException(anyref))

  def manifest(anyref: AnyRef): Manifest =
    persister(anyref).currentManifest

  def persist(anyref: AnyRef): Persisted[P] = {
    val p = persister(anyref)
    Persisted(p.currentManifest, p.persistAny(anyref))
  }

  def unpersist(persisted: Persisted[P]): AnyRef = unpersist(persisted.persisted, persisted.manifest)
  def unpersist(persisted: AnyRef, manifest: Manifest): AnyRef = {
    persisters.find(_.canUnpersist(manifest))
              .map(_.unpersistAny(manifest, persisted))
              .getOrElse(throw UnsupportedDataException(manifest.key, manifest.version))
  }
  // format: ON

  def ++(other: Persisters[P]): Persisters[P] = Persisters(persisters ++ other.persisters)
}

object Persisters {
  def apply[T: ClassTag, P <: AnyRef, V <: Version: VersionInfo](persister: Persister[T, P, V]): Persisters[P] = apply(List(persister))
  def apply[P <: AnyRef](first: Persister[_, P, _], rest: Persister[_, P, _]*): Persisters[P] = apply(first :: rest.toList)
}
