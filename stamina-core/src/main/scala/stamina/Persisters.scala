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
  def canUnpersist(manifest: Manifest): Boolean = persisters.exists(_.canUnpersist(manifest))

  // format: OFF
  def manifest(anyref: AnyRef): Manifest = {
    persisters.find(_.canPersist(anyref))
              .map(_.currentManifest)
              .getOrElse(throw UnregisteredTypeException(anyref))
  }

  def persist(anyref: AnyRef): Array[Byte] = {
    persisters.find(_.canPersist(anyref))
              .map(_.persistAny(anyref))
              .getOrElse(throw UnregisteredTypeException(anyref))
  }

  def unpersist(manifest: Manifest, persisted: Array[Byte]): AnyRef = {
    persisters.find(_.canUnpersist(manifest))
              .map(_.unpersistAny(manifest, persisted))
              .getOrElse(throw UnsupportedDataException(manifest.key, manifest.version))
  }

  def persistAndWrap(anyref: AnyRef): Persisted = {
    persisters.find(_.canPersist(anyref))
              .map(p => Persisted(p.key, p.currentVersion, p.persistAny(anyref)))
              .getOrElse(throw UnregisteredTypeException(anyref))
  }

  def unpersist(persisted: Persisted): AnyRef = {
    val manifest = Manifest(persisted.key, persisted.version)
    persisters.find(_.canUnpersist(manifest))
              .map(_.unpersistAny(manifest, persisted.bytes.toArray))
              .getOrElse(throw UnsupportedDataException(manifest.key, manifest.version))
  }
  // format: ON

  def ++(other: Persisters): Persisters = Persisters(persisters ++ other.persisters)
}

object Persisters {
  def apply[T: ClassTag, V <: Version: VersionInfo](persister: Persister[T, V]): Persisters = apply(List(persister))
  def apply(first: Persister[_, _], rest: Persister[_, _]*): Persisters = apply(first :: rest.toList)
}
