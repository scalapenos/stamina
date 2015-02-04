import scala.reflect._
import scala.util.control._

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

  /**
   * A simple container holding a persistence key, a version number, and the raw
   * serialized bytes.
   */
  case class Persisted(key: String, version: Int, bytes: ByteString)

  /**
   *
   */
  abstract class Persister[T: ClassTag, V <: Version: VersionInfo](val key: String) {
    lazy val version = Version.numberFor[V]

    def canPersist(a: AnyRef): Boolean = toT(a).isDefined

    def persist(t: T): Persisted
    def canUnpersist(p: Persisted): Boolean
    def unpersist(persisted: Persisted): T

    private[stamina] def toT(any: AnyRef): Option[T] = any match {
      case t: T ⇒ Some(t)
      case _    ⇒ None
    }

    private[stamina] def persistAny(any: AnyRef): Persisted = toT(any).map(persist).getOrElse(throw new IllegalArgumentException(s"Persister"))
    private[stamina] def unpersistAny(persisted: Persisted): AnyRef = unpersist(persisted).asInstanceOf[AnyRef]
  }

  case class Persisters(persisters: List[Persister[_, _]]) {
    def canPersist(a: AnyRef): Boolean = persisters.exists(_.canPersist(a))
    def canUnpersist(p: Persisted): Boolean = persisters.exists(_.canUnpersist(p))

    def persist(a: AnyRef): Persisted = persisters.find(_.canPersist(a)).map(_.persistAny(a)).getOrElse(throw new IllegalArgumentException("Persisters no persister found"))
    def unpersist(persisted: Persisted): AnyRef = persisters.find(_.canUnpersist(persisted)).map(_.unpersistAny(persisted)).getOrElse(throw new IllegalArgumentException("Persisters no unpersister found"))

    def ++(other: Persisters): Persisters = Persisters(persisters ++ other.persisters)
  }

  object Persisters {
    def apply[T: ClassTag, V <: Version: VersionInfo](persister: Persister[T, V]): Persisters = apply(List(persister))
    def apply(first: Persister[_, _], rest: Persister[_, _]*): Persisters = apply(first :: rest.toList)
  }

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
