package stamina

import scala.reflect.ClassTag
import spray.json._

import migrations._

/**
 * An implementation of the stamina Persister that will use spray-json
 * to read/write serialized values. It supports a DSL for specifying
 * migrations to apply to older versions of persisted values to bring
 * them up to date with the latest version.
 *
 * The DSL allows for any migration function that can transform an
 * instance of JsValue to another, migrated instance of JsValue but
 * by far the best way to implement these migration functions is to
 * use the json-lenses library that comes with spray-json.
 *
 * @example
 * {{{
 * val p = persister[CartCreated, V3]("cart-created",
 *   from[V1]
 *     .to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
 *     .to[V3](_.update('timestamp ! set[Long](System.currentTimeMillis - 3600000L)))
 * )
 * }}}
 *
 */
package object json {
  /** Simple type alias for Migration[JsValue] */
  type JsonMigration = Migration[JsValue]

  /** Simple type alias for Migrator[JsValue, V] */
  type JsonMigrator[V <: Version] = Migrator[JsValue, V]

  /**
   * Creates a JsonMigrator[V1] that can function as a builder for
   * creating JsonMigrator[V2], etc. Its migration will be the identity
   * function so calling its migrate function will not have any effect.
   */
  def from[V <: V1: VersionInfo]: JsonMigrator[V] = migrations.from[JsValue, V]

  /**
   * Creates a JsonPersister[T, V1], i.e. a JsonPersister that will only persist
   * and unpersist version 1. Use this function to produce the initial persister
   * for a new domain class/event/entity.
   */
  def persister[T: RootJsonFormat: ClassTag](key: String): JsonPersister[T, V1] = new V1JsonPersister[T](key)

  /**
   * Creates a JsonPersister[T, V] where V is a version greater than V1.
   * It will always persist instances of T to version V but it will use the specified
   * JsonMigrator[V] to migrate any values older than version V to version V before
   * unpersisting them.
   */
  def persister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo: MigratableVersion](key: String, migrator: JsonMigrator[V]): JsonPersister[T, V] = new VnJsonPersister[T, V](key, migrator)

  private[json] def toJsonBytes[T](t: T)(implicit writer: RootJsonWriter[T]): ByteString = ByteString(writer.write(t).compactPrint)
  private[json] def fromJsonBytes[T](bytes: ByteString)(implicit reader: RootJsonReader[T]): T = reader.read(parseJson(bytes))
  private[json] def parseJson(bytes: ByteString): JsValue = JsonParser(ParserInput(bytes.toArray))
}

package json {
  /**
   * Simple abstract marker superclass to unify (and hide) the two internal Persister implementations.
   */
  sealed abstract class JsonPersister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo](key: String) extends Persister[T, V](key) {
    private[json] def cannotUnpersist(p: Persisted) =
      s"""JsonPersister[${implicitly[ClassTag[T]].runtimeClass.getSimpleName}, V${currentVersion}](key = "${key}") cannot unpersist data with key "${p.key}" and version ${p.version}."""
  }

  private[json] class V1JsonPersister[T: RootJsonFormat: ClassTag](key: String) extends JsonPersister[T, V1](key) {
    def persist(t: T): Persisted = Persisted(key, currentVersion, toJsonBytes(t))
    def unpersist(p: Persisted): T = {
      if (canUnpersist(p)) fromJsonBytes[T](p.bytes)
      else throw new IllegalArgumentException(cannotUnpersist(p))
    }
  }

  private[json] class VnJsonPersister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo: MigratableVersion](key: String, migrator: JsonMigrator[V]) extends JsonPersister[T, V](key) {
    private val toVersionNumber = implicitly[VersionInfo[V]].versionNumber

    override def canUnpersist(p: Persisted): Boolean = p.key == key && migrator.canMigrate(p.version, toVersionNumber)

    def persist(t: T): Persisted = Persisted(key, currentVersion, toJsonBytes(t))
    def unpersist(p: Persisted): T = {
      if (canUnpersist(p)) migrator.migrate(parseJson(p.bytes), p.version, toVersionNumber).convertTo[T]
      else throw new IllegalArgumentException(cannotUnpersist(p))
    }
  }
}
