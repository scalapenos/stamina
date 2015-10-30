package stamina
package codec

/**
 * The encoding used to translate an instance of <code>Persisted</code>
 * to a byte array and back.
 */
trait PersistedCodec[P <: AnyRef] {
  def identifier: Int
  def writePersisted(persisted: Persisted[P]): Array[Byte]
  def readPersisted(bytes: Array[Byte]): Persisted[P]
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
object DefaultPersistedCodec extends PersistedCodec[Array[Byte]] {
  implicit val byteOrder = java.nio.ByteOrder.LITTLE_ENDIAN
  import java.nio.charset.StandardCharsets._

  def identifier = 490303

  def writePersisted(persisted: Persisted[Array[Byte]]): Array[Byte] = {
    val keyBytes = persisted.key.getBytes(UTF_8)

    ByteString.
      newBuilder.
      putInt(keyBytes.length).
      putBytes(keyBytes).
      putInt(persisted.version).
      append(ByteString(persisted.persisted)).
      result.
      toArray
  }

  def readPersisted(byteArray: Array[Byte]): Persisted[Array[Byte]] = {
    val bytes = ByteString(byteArray)
    val keyLength = bytes.take(4).iterator.getInt
    val (keyBytes, rest) = bytes.drop(4).splitAt(keyLength)
    val (versionBytes, data) = rest.splitAt(4)
    val key = keyBytes.utf8String
    val version = versionBytes.iterator.getInt

    Persisted(key, version, data)
  }
}
