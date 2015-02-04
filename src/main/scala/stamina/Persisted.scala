package stamina

/**
 * A simple container holding a persistence key, a version number,
 * and the associated serialized bytes.
 */
case class Persisted(key: String, version: Int, bytes: ByteString)
