package stamina

import akka.serialization._

/**
 * A custom Akka Serializer specifically designed for use with Akka Persistence.
 *
 * Key and version information is encoded in the manifest.
 */
abstract class StaminaAkkaSerializer private[stamina] (persisters: Persisters[Array[Byte]]) extends SerializerWithStringManifest {
  def this(persisters: List[Persister[_, Array[Byte], _]]) = this(Persisters(persisters))
  def this(persister: Persister[_, Array[Byte], _], persisters: Persister[_, Array[Byte], _]*) = this(Persisters(persister :: persisters.toList))

  /** Uniquely identifies this Serializer. */
  val identifier = 490304

  def manifest(obj: AnyRef): String =
    persisters.manifest(obj).manifest

  /**
   * @throws UnregisteredTypeException when the specified object is not supported by the persisters.
   */
  def toBinary(obj: AnyRef): Array[Byte] = {
    if (!persisters.canPersist(obj)) throw UnregisteredTypeException(obj)

    persisters.persist(obj).persisted
  }

  /**
   * @throws UnsupportedDataException when the persisted key and/or version is not supported.
   * @throws UnrecoverableDataException when the key and version are supported but recovery throws an exception.
   */
  def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    if (manifest.isEmpty) throw new IllegalArgumentException("No manifest found")
    val m = Manifest(manifest)
    if (!persisters.canUnpersist(m)) throw UnsupportedDataException(m.key, m.version)

    persisters.unpersist(Persisted(m, bytes))
  }
}
