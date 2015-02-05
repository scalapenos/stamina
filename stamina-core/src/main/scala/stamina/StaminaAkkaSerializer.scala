package stamina

import scala.util._
import akka.serialization._

/**
 * A custom Akka Serializer specifically designed for use with Akka Persistence.
 */
abstract class StaminaAkkaSerializer(persisters: Persisters, encoding: PersistedEncoding = DefaultPersistedEncoding) extends Serializer {
  /** We don't need class manifests since we're using keys to identify types. */
  val includeManifest: Boolean = false

  /** Uniquely identifies this Serializer by combining the encoding with a unique number. */
  val identifier = 42 * encoding.identifier

  /**
   * @throws UnregistredTypeException when the specified object is not supported by the persisters.
   */
  def toBinary(obj: AnyRef): Array[Byte] = {
    if (!persisters.canPersist(obj)) throw UnregistredTypeException(obj)

    encoding.writePersisted(persisters.persist(obj))
  }

  /**
   * @throws UnsupportedDataException when the persisted key and/or version is not supported.
   * @throws UnrecoverableDataException when the key and version are supported but recovery throws an exception.
   */
  def fromBinary(bytes: Array[Byte], clazz: Option[Class[_]]): AnyRef = {
    val persisted = encoding.readPersisted(bytes)

    if (!persisters.canUnpersist(persisted)) throw UnsupportedDataException(persisted)

    persisters.unpersist(persisted)
  }
}
