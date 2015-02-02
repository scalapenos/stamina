import scala.reflect._

package object stamina {
  type ToPersisted = PartialFunction[AnyRef, Persisted]
  type FromPersisted = PartialFunction[Persisted, AnyRef]

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
   * serialized/encoded bytes.
   */
  case class Persisted(key: String, version: Int, bytes: ByteString)

  object Persisted {
    /** The default version is 1. */
    def apply(key: String, bytes: ByteString): Persisted = apply(key, 1, bytes)
  }

  /**
   *
   */
  abstract class Persister2[T: ClassTag, V <: Version: VersionInfo](val key: String) {
    lazy val version = Version.numberFor[V]

    def canPersist(a: AnyRef): Boolean = a match {
      case t: T ⇒ true
      case _    ⇒ false
    }

    def canUnpersist(p: Persisted): Boolean

    def persist(t: T): Persisted
    def unpersist(persisted: Persisted): T
  }

  case class Persisters(persisters: List[Persister2[_, _]]) {
    def canPersist(a: AnyRef): Boolean = persisters.exists(_.canPersist(a))
    def canUnpersist(p: Persisted): Boolean = persisters.exists(_.canUnpersist(p))

    // def persist(a: AnyRef): Persisted = persisters.find(_.canPersist(a)).map(_.persist(a)).getOrElse(throw new IllegalArgumentException("Persisters.persist"))
    // def unpersist(persisted: Persisted): T
  }

  object Persisters {
    def apply[T: ClassTag, V <: Version: VersionInfo](persister: Persister2[T, V]): Persisters = apply(List(persister))
    def apply(first: Persister2[_, _], rest: Persister2[_, _]*): Persisters = apply(first :: rest.toList)
  }

  /**
   *
   */
  @deprecated(message = "Don't use it!", since = "recently")
  case class Persister(toPersisted: ToPersisted, fromPersisted: FromPersisted) {
    def ||(other: Persister) = orElse(other)
    def orElse(other: Persister): Persister = Persister(
      this.toPersisted orElse other.toPersisted,
      this.fromPersisted orElse other.fromPersisted
    )

    def canPersist(obj: AnyRef): Boolean = toPersisted.isDefinedAt(obj)
    def canRecover(persisted: Persisted): Boolean = fromPersisted.isDefinedAt(persisted)
  }

  object Persister {
    def apply(first: Persister, rest: Persister*): Persister = {
      rest.foldLeft(first)((persisters, persister) ⇒ persisters orElse persister)
    }
  }

  case class UnregistredTypeException(obj: AnyRef)
    extends RuntimeException(s"No persister registered for class: ${obj.getClass}")

  case class UnregisteredKeyException(key: String)
    extends RuntimeException(s"No persister registered for key: ${key}")

  case class UnrecoverableDataException(persisted: Persisted, error: Throwable)
    extends RuntimeException(s"Error while trying to read persisted data with key '${persisted.key}' and version ${persisted.version}. Cause: ${error}")
}
