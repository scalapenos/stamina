package stamina

import scala.reflect.ClassTag
import spray.json._

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
 * Example:
 * <pre>
 * val p = persister[CartCreated, V3]("cart-created",
 *   from[V1]
 *     .to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
 *     .to[V3](_.update('timestamp ! set[Long](System.currentTimeMillis - 3600000L)))
 * )
 * </pre>
 *
 */
package object json {
  /**
   * A JsonMigration is a simple type alias for a function that takes a JsValue
   * and produces another JsValue, probably transformed in some way.
   */
  type JsonMigration = JsValue ⇒ JsValue

  object JsonMigration {
    /** The Identity JsonMigration will always return its input as its output. */
    val Identity: JsonMigration = identity[JsValue]
  }

  /**
   * Adds support for combining two JsonMigrations into a new JsonMigration
   * that will apply the first one and then the second one.
   */
  implicit class JsonMigrationWithComposition(val firstMigration: JsonMigration) extends AnyVal {
    import JsonMigration._
    def &&(secondMigration: JsonMigration): JsonMigration = {
      if (firstMigration == Identity) secondMigration
      else if (secondMigration == Identity) firstMigration
      else (value: JsValue) ⇒ secondMigration(firstMigration(value))
    }
  }

  /**
   * Creates a JsonMigrator[V1] that can function as a builder for
   * creating JsonMigrator[V2], etc. Its migration will be the identity
   * function so calling its migrate function will not have any effect.
   */
  def from[V <: V1: VersionInfo]: JsonMigrator[V] = new JsonMigrator[V](Map(Version.numberFor[V] -> JsonMigration.Identity))

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
  def persister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo: Migratable](key: String, migrator: JsonMigrator[V]): JsonPersister[T, V] = new VnJsonPersister[T, V](key, migrator)

  private[json] def toJsonBytes[T](t: T)(implicit writer: RootJsonWriter[T]): ByteString = ByteString(writer.write(t).compactPrint)
  private[json] def fromJsonBytes[T](bytes: ByteString)(implicit reader: RootJsonReader[T]): T = reader.read(parseJson(bytes))
  private[json] def parseJson(bytes: ByteString): JsValue = JsonParser(ParserInput(bytes.toArray))
}

package json {
  case class MissingJsonMigrationException(fromVersion: Int, toVersion: Int)
    extends RuntimeException(s"No migration defined from version ${fromVersion} to version ${toVersion}.")

  /**
   * A <code>JsonMigrator[V]</code> can migrate values from older
   * versions to version <code>V</code> by applying a specific
   * <code>JsonMigrstion</code> to it.
   *
   * You can create instances of <code>JsonMigrator</code> by using
   * a small type-safe DSL consisting of two parts: the
   * <code>from[V1]</code> function will create a
   * <code>JsonMigrator[V1]</code> and then you can use the
   * <code>to[V](migration: JsonMigration)</code> function to build
   * instances that can migrate multiple versions.
   *
   * Example:
   * <pre>
   * val p = persister[CartCreated, V3]("cart-created",
   *   from[V1]
   *     .to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
   *     .to[V3](_.update('timestamp ! set[Long](System.currentTimeMillis - 3600000L)))
   * )
   * </pre>
   */
  final class JsonMigrator[V <: Version: VersionInfo] private[json] (migrations: Map[Int, JsonMigration] = Map.empty) {
    def canMigrate(fromVersion: Int): Boolean = migrations.contains(fromVersion)

    def migrate(json: JsValue, fromVersion: Int): JsValue = {
      migrations.get(fromVersion).map(_.apply(json)).getOrElse(
        throw MissingJsonMigrationException(fromVersion, Version.numberFor[V])
      )
    }

    def to[NextV <: Version: VersionInfo](migration: JsonMigration)(implicit isNextAfter: IsNextAfter[NextV, V]) = {
      new JsonMigrator[NextV](
        migrations.mapValues(_ && migration) + (Version.numberFor[NextV] -> JsonMigration.Identity)
      )
    }
  }

  /**
   * Simple abstract marker superclass to unify the two internal implementations.
   */
  sealed abstract class JsonPersister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo](key: String) extends Persister[T, V](key) {
    private[json] def self = s"""JsonPersister[${implicitly[ClassTag[T]].runtimeClass.getSimpleName}, V${currentVersion}](key = "${key}")"""
  }

  private[json] class V1JsonPersister[T: RootJsonFormat: ClassTag](key: String) extends JsonPersister[T, V1](key) {
    def persist(t: T): Persisted = Persisted(key, currentVersion, toJsonBytes(t))
    def unpersist(p: Persisted): T = {
      if (canUnpersist(p)) fromJsonBytes[T](p.bytes)
      else throw new IllegalArgumentException(s"""$self cannot unpersist data with key "${p.key}" and version ${p.version}.""")
    }
  }

  private[json] class VnJsonPersister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo: Migratable](key: String, migrator: JsonMigrator[V]) extends JsonPersister[T, V](key) {
    override def canUnpersist(p: Persisted): Boolean = p.key == key && migrator.canMigrate(p.version)

    def persist(t: T): Persisted = Persisted(key, currentVersion, toJsonBytes(t))
    def unpersist(p: Persisted): T = {
      if (canUnpersist(p)) migrator.migrate(parseJson(p.bytes), p.version).convertTo[T]
      else throw new IllegalArgumentException(s"""$self cannot unpersist data with key "${p.key}" and version ${p.version}.""")
    }
  }
}
