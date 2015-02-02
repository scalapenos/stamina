package stamina
package json

object Versions {
  sealed abstract class Version

  @annotation.implicitNotFound(msg = "Cannot find VersionInfo type class for ${V}")
  abstract class VersionInfo[V <: Version](val versionNumber: Int)

  @annotation.implicitNotFound(msg = "Cannot find proof that ${V} is a migratable version (i.e. it is higher than V1)")
  sealed trait Migratable[V <: Version]

  @annotation.implicitNotFound(msg = "Cannot find proof that ${A} is the next version after ${B}")
  sealed trait IsNextAfter[A <: Version, B <: Version]

  def versionNumber[V <: Version: VersionInfo]: Int = implicitly[VersionInfo[V]].versionNumber

  class V1 extends Version
  case object V1 extends V1 {
    implicit object Info extends VersionInfo[V1](1)
  }

  class V2 extends Version
  case object V2 extends V2 {
    implicit object Info extends VersionInfo[V2](2) with Migratable[V2] with IsNextAfter[V2, V1]
  }

  class V3 extends Version
  case object V3 extends V3 {
    implicit object Info extends VersionInfo[V3](3) with Migratable[V3] with IsNextAfter[V3, V2]
  }
}

object SprayJsonPersistence {
  import scala.reflect.ClassTag
  import spray.json._
  import Versions._

  type JsonMigration = JsValue ⇒ JsValue
  object JsonMigration {
    val Identity: JsonMigration = identity[JsValue]
  }

  implicit class JsonMigrationWithComposition(val migration: JsonMigration) extends AnyVal {
    def &&(migration2: JsonMigration): JsonMigration = (value: JsValue) ⇒ migration2(migration(value))
  }

  class JsonMigrator[V <: Version: VersionInfo](migrations: Map[Int, JsonMigration] = Map.empty) {
    def migrate(json: JsValue, fromVersion: Int): JsValue = {
      migrations.get(fromVersion).map(_.apply(json)).getOrElse(
        throw new IllegalArgumentException(s"No migration defined from version $fromVersion to version ...")
      )
    }

    def to[HigherV <: Version: VersionInfo](migration: JsonMigration)(implicit isHigherThan: IsNextAfter[HigherV, V]): JsonMigrator[HigherV] = {
      val updatedOldMigrations: Map[Int, JsonMigration] = migrations.mapValues(_ && migration)
      val newMigrations = updatedOldMigrations + (versionNumber[HigherV] -> JsonMigration.Identity)

      new JsonMigrator[HigherV](newMigrations)
    }
  }

  def from[V <: V1: VersionInfo] = new JsonMigrator[V](Map(versionNumber[V] -> JsonMigration.Identity))

  abstract class JsonPersister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo](key: String) {
    protected lazy val version = versionNumber[V]

    def canPersist(a: AnyRef): Boolean = a match {
      case t: T ⇒ true
      case _    ⇒ false
    }

    def canUnpersist(p: Persisted): Boolean = p.key == key && p.version == version

    def persist(t: T): Persisted
    def unpersist(persisted: Persisted): T
  }

  class V1JsonPersister[T: RootJsonFormat: ClassTag, V <: V1: VersionInfo](key: String) extends JsonPersister[T, V](key) {
    def persist(t: T): Persisted = Persisted(key, version, t.toJsonBytes)
    def unpersist(p: Persisted): T = {
      if (p.key == key && p.version == version) p.bytes.fromJsonBytes[T]
      else throw new IllegalArgumentException(s"V1JsonPersister: $p.key was not equal to $key and/or $p.version was not equal to ${version}.")
    }
  }

  def persister[T: RootJsonFormat: ClassTag](key: String): JsonPersister[T, V1] = new V1JsonPersister[T, V1](key)

  class VnJsonPersister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo: Migratable](key: String, migrator: JsonMigrator[V]) extends JsonPersister[T, V](key) {
    def persist(t: T): Persisted = Persisted(key, version, t.toJsonBytes)
    def unpersist(p: Persisted): T = {
      if (p.key != key) throw new IllegalArgumentException(s"VnJsonPersister: ${p.key} was not equal to ${key}.")
      else {
        migrator.migrate(p.bytes.parseJson, p.version).convertTo[T]
      }
    }
  }

  def persister[T: RootJsonFormat: ClassTag, V <: Version: VersionInfo: Migratable](key: String, migrator: JsonMigrator[V]): JsonPersister[T, V] = new VnJsonPersister[T, V](key, migrator)
}
