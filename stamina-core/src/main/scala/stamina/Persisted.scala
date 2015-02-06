package stamina

/**
 * A simple container holding a persistence key, a version number,
 * and the associated serialized bytes.
 */
case class Persisted(key: String, version: Int, bytes: ByteString)

object Persisted {
  def apply(key: String, version: Int, bytes: Array[Byte]): Persisted = apply(key, version, ByteString(bytes))
}
