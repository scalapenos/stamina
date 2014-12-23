package stamina.old

import java.nio.charset.StandardCharsets._
import scala.util._

import akka.serialization._

case class SerializationException(message: String) extends RuntimeException(message)
case class DeserializationException(message: String) extends RuntimeException(message)

/**
 * A custom Akka Serializer specifically designed for use with Akka Persistence.
 *
 * It delegates the actual serialization and deserialization out to other code
 * but it stores that serialized data with a key and it provides support for
 * creation simple migration paths.
 */
abstract class AkkaPersistenceMetaSerializer extends Serializer {
  private implicit val byteOrder = java.nio.ByteOrder.LITTLE_ENDIAN

  /** Implement this to provider your custom serializers. */
  def serializers: Serializers

  /** Implement this to provider your custom deserializers. */
  def deserializers: Deserializers

  /** This is whether "fromBinary" requires a "clazz" or not. */
  val includeManifest: Boolean = false

  /** Uniquely identifies this Serializer */
  val identifier = 723546893

  def toBinary(obj: AnyRef): Array[Byte] = {
    if (!serializers.isDefinedAt(obj)) throw new SerializationException(s"No serializer defined for ${obj.getClass.getName}.")

    val serialized = serializers(obj)
    val keyBytes = serialized.key.getBytes(UTF_8)

    // Write the following structure:
    // length of key (4 bytes), key bytes (n bytes), serialized bytes (n bytes)
    ByteString.
      newBuilder.
      putInt(keyBytes.length).
      putBytes(keyBytes).
      append(serialized.bytes).
      result.
      toArray
  }

  def fromBinary(byteArray: Array[Byte], clazz: Option[Class[_]]): AnyRef = {
    // Read the following structure:
    // length of key (4 bytes), key bytes (n bytes), serialized bytes (n bytes)
    val bytes = ByteString(byteArray)
    val keyLength = bytes.take(4).iterator.getInt
    val (key, data) = bytes.drop(4).splitAt(keyLength)

    Try(deserializers(Serialized(key.utf8String, data))) match {
      case Success(deserialized) ⇒ deserialized
      case Failure(error)        ⇒ throw new DeserializationException(s"""Unable to deserialize bytes for key "${key.utf8String}". Details: ${error.getClass.getSimpleName}, ${error.getMessage}""")
    }
  }
}

