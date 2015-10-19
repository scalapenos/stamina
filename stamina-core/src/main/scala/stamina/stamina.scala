
package object stamina {
  /** Type alias for Akka's ByteSttring so we don't have to import it everywhere. */
  type ByteString = akka.util.ByteString
  val ByteString = akka.util.ByteString
}

/**
 *
 */
package stamina {
  /**
   * Marker trait for classes that should be persisted using the StaminaAkkaSerializer.
   *
   * This marker trait can be used to mark all your top-level persistable classes
   * (i.e. events, snapshots, etc.) so that you will only need a few lines of
   * configuration in your application.conf, namely:
   * {{{
   * akka.actor.serializers.stamina = <FQCN of your subclass of StaminaAkkaSerializer>
   * akka.actor.serialization-bindings {
   *   "stamina.Persistable" = stamina
   * }
   * }}}
   */
  trait Persistable extends java.io.Serializable

  import scala.util.control._

  case class UnregisteredTypeException(obj: AnyRef)
    extends RuntimeException(s"No persister registered for class: ${obj.getClass}")
    with NoStackTrace

  case class UnsupportedDataException(key: String, version: Int)
    extends RuntimeException(s"No unpersister registered for key: '$key' and version: $version")
    with NoStackTrace

  case class UnrecoverableDataException(manifest: Manifest, error: Throwable)
    extends RuntimeException(s"Error while trying to unpersist data with key '${manifest.key}' and version ${manifest.version}. Cause: ${error}")
    with NoStackTrace

  case class Manifest(manifest: String) {
    lazy val key: String = manifest.substring(manifest.indexOf('-') + 1)
    lazy val version: Int = Integer.valueOf(manifest.substring(0, manifest.indexOf('-')))
  }
  object Manifest {
    def apply(key: String, version: Int): Manifest = Manifest(version + "-" + key)
  }
}
