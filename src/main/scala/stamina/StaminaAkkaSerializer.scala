package stamina

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
 *
 */
final class StaminaAkkaSerializer(persisters: Persister) extends Serializer with HardcodedByteOrder {
  import StaminaAkkaSerializer._

  def this(first: Persister, rest: Persister*) =
    this(rest.foldLeft(first)((persisters, persister) ⇒ persisters orElse persister))

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
   * @throws UnregistredTypeException when the specified object is not supported by the persisters.
   */
  def toBinary(obj: AnyRef): Array[Byte] = {
    if (!persisters.canPersist(obj)) throw UnregistredTypeException(obj)

    StaminaAkkaSerializer.toBinary(persisters.toPersisted(obj))
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

    if (!persisters.canRecover(persisted)) throw UnregisteredKeyException(key)

    Try(persisters.fromPersisted(persisted)) match {
      case Success(deserialized) ⇒ deserialized
      case Failure(error)        ⇒ throw new UnrecoverableDataException(persisted, error)
    }
  }
}

object StaminaAkkaSerializer extends HardcodedByteOrder {
  def apply(persisters: Persister): StaminaAkkaSerializer = new StaminaAkkaSerializer(persisters)
  def apply(first: Persister, rest: Persister*): StaminaAkkaSerializer = new StaminaAkkaSerializer(first, rest: _*)

  import java.nio.charset.StandardCharsets._

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
