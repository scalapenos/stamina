
package object stamina {
  /**  */
  type ByteString = akka.util.ByteString
  val ByteString = akka.util.ByteString
}

package stamina {
  /**
   * Marker trait for classes that need to be persisted using Akka Persistence.
   * Used by the Akka serialization config to indicate which serializer to use for
   * persistentable classes.
   */
  trait Persistable extends java.io.Serializable

  /**
   *
   */
  case class Persisted(key: String, version: Int, bytes: ByteString)

  /**
   *
   */
  trait Persister {
    def key: String
    def toPersisted(t: AnyRef): Persisted
    def fromPersisted(persisted: Persisted): AnyRef
  }

  case class UnregistredClassException(obj: AnyRef)
    extends RuntimeException(s"No persister registered for class: ${obj.getClass}")

  case class UnregisteredKeyException(key: String)
    extends RuntimeException(s"No persister registered for key: ${key}")

  case class UnreadableDataException(persisted: Persisted, error: Throwable)
    extends RuntimeException(s"Error while trying to read persisted data with key '${persisted.key}' and version ${persisted.version}. Cause: ${error}")
}
