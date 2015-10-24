package stamina

/**
 * A simple container holding a persistence key, a version number,
 * and the associated serialized bytes.
 */
case class Persisted(key: String, version: Int, bytes: Array[Byte]) {
  lazy val manifest = Manifest(key, version)
}

object Persisted {
  def apply(manifest: Manifest, bytes: Array[Byte]): Persisted = apply(manifest.key, manifest.version, bytes)
  def apply(key: String, version: Int, bytes: ByteString): Persisted = apply(key, version, bytes.toArray)
}
