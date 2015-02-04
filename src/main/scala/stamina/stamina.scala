
package object stamina {
  /**  */
  type ByteString = akka.util.ByteString
  val ByteString = akka.util.ByteString
}

package stamina {
  /**
   * Marker trait for classes that should be persisted using the StaminaAkkaSerializer.
   *
   * Unforntunately we still need to indicate to Akka which classes should be persisted
   * with which Serializer. This marker trait can be used to mark all your top-level
   * persistable classes (i.e. events, snapshots, etc.) so that you will only need a few
   * lines of configuration in your application.conf, namely:
   *
   * akka.actor.serializers.stamina = <FQCN of your subclass of StaminaAkkaSerializer>
   * akka.actor.serialization-bindings {
   *   "stamina.Persistable" = stamina
   * }
   *
   */
  trait Persistable extends java.io.Serializable

  import scala.util.control._

  case class UnregistredTypeException(obj: AnyRef)
    extends RuntimeException(s"No persister registered for class: ${obj.getClass}")
    with NoStackTrace

  case class UnregisteredKeyException(key: String)
    extends RuntimeException(s"No persister registered for key: ${key}")
    with NoStackTrace

  case class UnrecoverableDataException(persisted: Persisted, error: Throwable)
    extends RuntimeException(s"Error while trying to read persisted data with key '${persisted.key}' and version ${persisted.version}. Cause: ${error}")
    with NoStackTrace
}
