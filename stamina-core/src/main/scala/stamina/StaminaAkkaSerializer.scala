package stamina

import akka.serialization._

/**
 * A custom Akka Serializer specifically designed for use with Akka Persistence.
 */
abstract class StaminaAkkaSerializer private[stamina] (persisters: Persisters) extends SerializerWithStringManifest {
  def this(persisters: List[Persister[_, _]]) = this(Persisters(persisters))
  def this(persister: Persister[_, _], persisters: Persister[_, _]*) = this(Persisters(persister :: persisters.toList))

  /** Uniquely identifies this Serializer. */
  val identifier = 490304

  def manifest(obj: AnyRef): String =
    persisters.manifest(obj)

  /**
   * @throws UnregisteredTypeException when the specified object is not supported by the persisters.
   */
  def toBinary(obj: AnyRef): Array[Byte] = {
    if (!persisters.canPersist(obj)) throw UnregisteredTypeException(obj)

    persisters.persist(obj)
  }

  /**
   * @throws UnsupportedDataException when the persisted key and/or version is not supported.
   * @throws UnrecoverableDataException when the key and version are supported but recovery throws an exception.
   */
  def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    if (manifest.isEmpty) throw new IllegalArgumentException("No manifest found")
    if (!persisters.canUnpersist(manifest)) throw UnsupportedDataException(Manifest.key(manifest), Manifest.version(manifest))

    persisters.unpersist(manifest, bytes)
  }
}
