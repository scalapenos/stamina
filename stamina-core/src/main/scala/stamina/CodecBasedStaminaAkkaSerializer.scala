package stamina

import akka.serialization._

/**
 * A custom Akka Serializer specifically designed for use with Akka Persistence.
 */
abstract class CodecBasedStaminaAkkaSerializer private[stamina] (persisters: Persisters, codec: PersistedCodec) extends Serializer {
  def this(persisters: List[Persister[_, _]], codec: PersistedCodec = DefaultPersistedCodec) = this(Persisters(persisters), codec)
  def this(persister: Persister[_, _], persisters: Persister[_, _]*) = this(Persisters(persister :: persisters.toList), DefaultPersistedCodec)

  /** We don't need class manifests since we're using keys to identify types. */
  val includeManifest: Boolean = false

  /** Uniquely identifies this Serializer by combining the codec with a unique number. */
  val identifier = 42 * codec.identifier

  /**
   * @throws UnregisteredTypeException when the specified object is not supported by the persisters.
   */
  def toBinary(obj: AnyRef): Array[Byte] = {
    if (!persisters.canPersist(obj)) throw UnregisteredTypeException(obj)

    codec.writePersisted(persisters.persistAndWrap(obj))
  }

  /**
   * @throws UnsupportedDataException when the persisted key and/or version is not supported.
   * @throws UnrecoverableDataException when the key and version are supported but recovery throws an exception.
   */
  def fromBinary(bytes: Array[Byte], clazz: Option[Class[_]]): AnyRef = {
    val persisted = codec.readPersisted(bytes)
    val manifest = Manifest.encode(persisted.key, persisted.version)

    if (!persisters.canUnpersist(manifest)) throw UnsupportedDataException(persisted.key, persisted.version)

    persisters.unpersist(manifest, persisted.bytes.toArray)
  }
}
