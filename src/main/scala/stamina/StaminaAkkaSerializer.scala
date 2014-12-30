package stamina

import java.nio.charset.StandardCharsets._

import scala.reflect._
import scala.util._

import akka.serialization._

/** We need one for serializing. */
trait HardcodedByteOrder {
  implicit val byteOrder = java.nio.ByteOrder.LITTLE_ENDIAN
}

/**
 * A custom Akka Serializer specifically designed for use with Akka Persistence.
 *
 * It delegates the actual serialization and deserialization out to other code
 * but it stores that serialized data with a key and a version.
 */
abstract class StaminaAkkaSerializer extends Serializer with HardcodedByteOrder {
  import StaminaAkkaSerializer._

  /**
   * Subclasses should provide a composite instance of Persister to handle
   * the actual (de)serialization.
   */
  def persister: Persister

  /** We ddon't need class manifests since we're using more flexible keys. */
  val includeManifest: Boolean = false

  /** Uniquely identifies this Serializer */
  val identifier = 983543153

  /**
   * Writes the following structure:
   *
   *   - length of key (4 bytes),
   *   - key bytes (n bytes),
   *   - version (4 bytes)
   *   - persisted data (n bytes)
   *
   * @throws UnregistredTypeException when the specified object is not supported by the persister.
   */
  def toBinary(obj: AnyRef): Array[Byte] = {
    if (!persister.canPersist(obj)) throw UnregistredTypeException(obj)

    StaminaAkkaSerializer.toBinary(persister.toPersisted(obj))
  }

  /**
   * Reads the following structure:
   *
   *   - length of key (4 bytes),
   *   - key bytes (n bytes),
   *   - version (4 bytes)
   *   - persisted data (n bytes)
   *
   * @throws UnregisteredKeyException when the persisted key is not recognized.
   * @throws UnrecoverableDataException when the key is supported but recovery throws an exception.
   */
  def fromBinary(byteArray: Array[Byte], clazz: Option[Class[_]]): AnyRef = {
    val bytes = ByteString(byteArray)
    val keyLength = bytes.take(4).iterator.getInt
    val (keyBytes, rest) = bytes.drop(4).splitAt(keyLength)
    val (versionBytes, data) = rest.splitAt(4)
    val key = keyBytes.utf8String
    val version = versionBytes.iterator.getInt
    val persisted = Persisted(key, version, data)

    if (!persister.canRecover(persisted)) throw UnregisteredKeyException(key)

    Try(persister.fromPersisted(persisted)) match {
      case Success(deserialized) ⇒ deserialized
      case Failure(error)        ⇒ throw new UnrecoverableDataException(persisted, error)
    }
  }
}

object StaminaAkkaSerializer extends HardcodedByteOrder {
  private[stamina] def toBinary(persisted: Persisted): Array[Byte] = {
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
}
