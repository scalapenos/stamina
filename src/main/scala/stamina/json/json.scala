package stamina

import scala.reflect.ClassTag
import spray.json._

/**
 * Adds some handy sugar around reading/writing json from/to ByteStrings.
 */
package object json {
  implicit class AnyWithJsonByteStringConversion[T](val any: T) extends AnyVal {
    def toJsonBytes(implicit writer: RootJsonWriter[T]): ByteString = ByteString(writer.write(any).compactPrint)
  }

  implicit class ByteStringWithRootJsonReaderSupport(val bytes: ByteString) extends AnyVal {
    def parseJson = JsonParser(ParserInput(bytes.toArray))
    def fromJsonBytes[T](implicit reader: RootJsonReader[T]): T = reader.read(parseJson)
  }

  type JsonMigration = JsValue ⇒ JsValue
  object JsonMigration {
    val Identity: JsonMigration = identity[JsValue]
  }

  implicit class JsonMigrationWithComposition(val migration: JsonMigration) extends AnyVal {
    def &&(migration2: JsonMigration): JsonMigration = (value: JsValue) ⇒ migration2(migration(value))
  }

  def from[V <: V1: VersionInfo] = new JsonMigrator[V](Map(Version.numberFor[V] -> JsonMigration.Identity))

  def persister[T: RootJsonFormat: ClassTag](key: String): JsonPersister[T, V1] = new V1JsonPersister[T, V1](key)
  def persister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo: Migratable](key: String, migrator: JsonMigrator[V]): JsonPersister[T, V] = new VnJsonPersister[T, V](key, migrator)
}

package json {
  class JsonMigrator[V <: Version: VersionInfo](migrations: Map[Int, JsonMigration] = Map.empty) {
    def canMigrate(fromVersion: Int): Boolean = migrations.contains(fromVersion)

    def migrate(json: JsValue, fromVersion: Int): JsValue = {
      migrations.get(fromVersion).map(_.apply(json)).getOrElse(
        throw new IllegalArgumentException(s"No migration defined from version $fromVersion to version ...")
      )
    }

    def to[HigherV <: Version: VersionInfo](migration: JsonMigration)(implicit isHigherThan: IsNextAfter[HigherV, V]): JsonMigrator[HigherV] = {
      val updatedOldMigrations: Map[Int, JsonMigration] = migrations.mapValues(_ && migration)
      val newMigrations = updatedOldMigrations + (Version.numberFor[HigherV] -> JsonMigration.Identity)

      new JsonMigrator[HigherV](newMigrations)
    }
  }

  abstract class JsonPersister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo](key: String) extends Persister[T, V](key)

  private[json] class V1JsonPersister[T: RootJsonFormat: ClassTag, V <: V1: VersionInfo](key: String) extends JsonPersister[T, V](key) {
    def canUnpersist(p: Persisted): Boolean = p.key == key && p.version == version

    def persist(t: T): Persisted = Persisted(key, version, t.toJsonBytes)
    def unpersist(p: Persisted): T = {
      if (p.key == key && p.version == version) p.bytes.fromJsonBytes[T]
      else throw new IllegalArgumentException(s"V1JsonPersister: $p.key was not equal to $key and/or $p.version was not equal to ${version}.")
    }
  }

  private[json] class VnJsonPersister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo: Migratable](key: String, migrator: JsonMigrator[V]) extends JsonPersister[T, V](key) {
    def canUnpersist(p: Persisted): Boolean = p.key == key && migrator.canMigrate(p.version)

    def persist(t: T): Persisted = Persisted(key, version, t.toJsonBytes)
    def unpersist(p: Persisted): T = {
      if (p.key != key) throw new IllegalArgumentException(s"VnJsonPersister: ${p.key} was not equal to ${key}.")
      else {
        migrator.migrate(p.bytes.parseJson, p.version).convertTo[T]
      }
    }
  }

}
