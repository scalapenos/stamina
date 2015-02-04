package stamina

import scala.reflect.ClassTag
import spray.json._

/**
 * Adds some handy sugar around reading/writing json from/to ByteStrings.
 */
package object json {
  type JsonMigration = JsValue ⇒ JsValue
  object JsonMigration {
    val Identity: JsonMigration = identity[JsValue]
  }

  implicit class JsonMigrationWithComposition(val migration: JsonMigration) extends AnyVal {
    def &&(migration2: JsonMigration): JsonMigration = {
      if (migration == JsonMigration.Identity) migration2
      else if (migration2 == JsonMigration.Identity) migration
      else { (value: JsValue) ⇒
        if (migration == JsonMigration.Identity) migration2(value)
        else migration2(migration(value))
      }
    }
  }

  /**
   *
   */
  def from[V <: V1: VersionInfo] = new JsonMigrator[V](Map(Version.numberFor[V] -> JsonMigration.Identity))

  /**
   *
   */
  def persister[T: RootJsonFormat: ClassTag](key: String): JsonPersister[T, V1] = new V1JsonPersister[T, V1](key)

  /**
   *
   */
  def persister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo: Migratable](key: String, migrator: JsonMigrator[V]): JsonPersister[T, V] = new VnJsonPersister[T, V](key, migrator)

  private[json] def toJsonBytes[T](t: T)(implicit writer: RootJsonWriter[T]): ByteString = ByteString(writer.write(t).compactPrint)
  private[json] def fromJsonBytes[T](bytes: ByteString)(implicit reader: RootJsonReader[T]): T = reader.read(parseJson(bytes))
  private[json] def parseJson(bytes: ByteString): JsValue = JsonParser(ParserInput(bytes.toArray))
}

package json {
  class JsonMigrator[V <: Version: VersionInfo](migrations: Map[Int, JsonMigration] = Map.empty) {
    def canMigrate(fromVersion: Int): Boolean = migrations.contains(fromVersion)

    def migrate(json: JsValue, fromVersion: Int): JsValue = {
      migrations.get(fromVersion).map(_.apply(json)).getOrElse(
        throw new IllegalArgumentException(s"No migration defined from version $fromVersion to version ...")
      )
    }

    def to[NextV <: Version: VersionInfo](migration: JsonMigration)(implicit isNextAfter: IsNextAfter[NextV, V]): JsonMigrator[NextV] = {
      val updatedOldMigrations: Map[Int, JsonMigration] = migrations.mapValues(_ && migration)
      val newMigrations = updatedOldMigrations + (Version.numberFor[NextV] -> JsonMigration.Identity)

      new JsonMigrator[NextV](newMigrations)
    }
  }

  abstract class JsonPersister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo](key: String) extends Persister[T, V](key)

  private[json] class V1JsonPersister[T: RootJsonFormat: ClassTag, V <: V1: VersionInfo](key: String) extends JsonPersister[T, V](key) {
    def canUnpersist(p: Persisted): Boolean = p.key == key && p.version == version

    def persist(t: T): Persisted = Persisted(key, version, toJsonBytes(t))
    def unpersist(p: Persisted): T = {
      if (p.key == key && p.version == version) fromJsonBytes[T](p.bytes)
      else throw new IllegalArgumentException(s"V1JsonPersister: $p.key was not equal to $key and/or $p.version was not equal to ${version}.")
    }
  }

  private[json] class VnJsonPersister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo: Migratable](key: String, migrator: JsonMigrator[V]) extends JsonPersister[T, V](key) {
    def canUnpersist(p: Persisted): Boolean = p.key == key && migrator.canMigrate(p.version)

    def persist(t: T): Persisted = Persisted(key, version, toJsonBytes(t))
    def unpersist(p: Persisted): T = {
      if (p.key != key) throw new IllegalArgumentException(s"VnJsonPersister: ${p.key} was not equal to ${key}.")
      else {
        migrator.migrate(parseJson(p.bytes), p.version).convertTo[T]
      }
    }
  }

}
