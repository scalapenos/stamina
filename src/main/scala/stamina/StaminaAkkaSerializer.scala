package stamina

import scala.reflect._
import scala.util._

import akka.serialization._

/**
 * A custom Akka Serializer specifically designed for use with Akka Persistence.
 *
 *
 */
abstract class StaminaAkkaSerializer(persisters: Persisters, encoding: PersistedEncoding = DefaultPersistedEncoding) extends Serializer {
  /** We don't need class manifests since we're using keys to identify types. */
  val includeManifest: Boolean = false

  /** Uniquely identifies this Serializer by combining the encoding with a unique number. */
  val identifier = 9835 * encoding.identifier

  /**
   *
   *
   * @throws UnregistredTypeException when the specified object is not supported by the persisters.
   */
  def toBinary(obj: AnyRef): Array[Byte] = {
    if (!persisters.canPersist(obj)) throw UnregistredTypeException(obj)

    encoding.writePersisted(persisters.persist(obj))
  }

  /**
   *
   *
   * @throws UnregisteredKeyException when the persisted key is not recognized.
   * @throws UnrecoverableDataException when the key is supported but recovery throws an exception.
   */
  def fromBinary(bytes: Array[Byte], clazz: Option[Class[_]]): AnyRef = {
    val persisted = encoding.readPersisted(bytes)

    if (!persisters.canUnpersist(persisted)) throw UnregisteredKeyException(persisted.key)

    Try(persisters.unpersist(persisted)) match {
      case Success(deserialized) ⇒ deserialized
      case Failure(error)        ⇒ throw new UnrecoverableDataException(persisted, error)
    }
  }
}

trait PersistedEncoding {
  def identifier: Int
  def writePersisted(persisted: Persisted): Array[Byte]
  def readPersisted(bytes: Array[Byte]): Persisted
}

/**
 * Uses the following structure:
 *
 *   - length of key (4 bytes),
 *   - key bytes (n bytes),
 *   - version (4 bytes)
 *   - persisted data (n bytes)
 *
 */
object DefaultPersistedEncoding extends PersistedEncoding {
  implicit val byteOrder = java.nio.ByteOrder.LITTLE_ENDIAN
  import java.nio.charset.StandardCharsets._

  def identifier = 490303

  def writePersisted(persisted: Persisted): Array[Byte] = {
    val keyBytes = persisted.key.getBytes(UTF_8)

    ByteString.
      newBuilder.
      putInt(keyBytes.length).
      putBytes(keyBytes).
      putInt(persisted.version).
      append(persisted.bytes).
      result.
      toArray
  }

  def readPersisted(byteArray: Array[Byte]): Persisted = {
    val bytes = ByteString(byteArray)
    val keyLength = bytes.take(4).iterator.getInt
    val (keyBytes, rest) = bytes.drop(4).splitAt(keyLength)
    val (versionBytes, data) = rest.splitAt(4)
    val key = keyBytes.utf8String
    val version = versionBytes.iterator.getInt

    Persisted(key, version, data)
  }
}
