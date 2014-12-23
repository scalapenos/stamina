package stamina

import java.nio.charset.StandardCharsets._

import scala.reflect._
import scala.util._

import akka.serialization._

/**
 * A custom Akka Serializer specifically designed for use with Akka Persistence.
 *
 * It delegates the actual serialization and deserialization out to other code
 * but it stores that serialized data with a key and it provides support for
 * creation simple migration paths.
 */
abstract class AkkaPersistenceSerializer extends Serializer {
  private implicit val byteOrder = java.nio.ByteOrder.LITTLE_ENDIAN
  private val classToPersister = collection.mutable.Map.empty[Class[_], Persister]
  private val keyToPersister = collection.mutable.Map.empty[String, Persister]

  /**  */
  def register[T <: AnyRef: ClassTag](persister: Persister): Unit = {
    classToPersister += (implicitly[ClassTag[T]].runtimeClass -> persister)
    keyToPersister += (persister.key -> persister)
  }

  /** This is whether "fromBinary" requires a "clazz" or not. */
  val includeManifest: Boolean = false

  /** Uniquely identifies this Serializer */
  val identifier = 723546893

  def toBinary(obj: AnyRef): Array[Byte] = {
    if (!classToPersister.contains(obj.getClass)) throw UnregistredClassException(obj)

    val persisted = classToPersister(obj.getClass).toPersisted(obj)
    val keyBytes = persisted.key.getBytes(UTF_8)

    // Write the following structure:
    // length of key (4 bytes), key bytes (n bytes), persisted bytes (n bytes)
    ByteString.
      newBuilder.
      putInt(keyBytes.length).
      putBytes(keyBytes).
      putInt(persisted.version).
      append(persisted.bytes).
      result.
      toArray
  }

  def fromBinary(byteArray: Array[Byte], clazz: Option[Class[_]]): AnyRef = {
    // Read the following structure:
    // length of key (4 bytes), key bytes (n bytes), version (4 bytes), serialized bytes (n bytes)
    val bytes = ByteString(byteArray)
    val keyLength = bytes.take(4).iterator.getInt
    val (keyBytes, rest) = bytes.drop(4).splitAt(keyLength)
    val (versionBytes, data) = rest.splitAt(4)
    val key = keyBytes.utf8String
    val version = versionBytes.iterator.getInt
    val persisted = Persisted(key, version, data)

    if (!keyToPersister.contains(key)) throw UnregisteredKeyException(key)

    Try(keyToPersister(key).fromPersisted(persisted)) match {
      case Success(deserialized) ⇒ deserialized
      case Failure(error)        ⇒ throw new UnreadableDataException(persisted, error)
    }
  }
}

