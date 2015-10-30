package stamina

/**
 * A simple container holding a persistence key, a version number,
 * and the associated serialized bytes.
 */
case class Persisted[P <: AnyRef](key: String, version: Int, persisted: P) {
  lazy val manifest = Manifest(key, version)
}

object Persisted {
  def apply[P <: AnyRef](manifest: Manifest, persisted: P): Persisted[P] = apply(manifest.key, manifest.version, persisted)
  def apply(key: String, version: Int, bytes: ByteString): Persisted[Array[Byte]] = apply[Array[Byte]](key, version, bytes.toArray)
}
