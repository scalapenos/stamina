package stamina
package json

object Versions {
  sealed abstract class Version

  @annotation.implicitNotFound(msg = "Cannot find VersionInfo type class for ${V}")
  sealed trait VersionInfo[V <: Version] {
    def versionNumber: Int
  }

  def versionNumber[V <: Version: VersionInfo]: Int = implicitly[VersionInfo[V]].versionNumber

  abstract class VersionInfoImpl[V <: Version](val versionNumber: Int) extends VersionInfo[V]

  @annotation.implicitNotFound(msg = "Cannot find proof that ${A} is the next version after ${B}")
  sealed trait IsNextVersionAfter[A <: Version, B <: Version]

  class V1 extends Version
  case object V1 extends V1 {
    implicit object Info extends VersionInfoImpl[V1](1)
  }

  class V2 extends Version
  case object V2 extends V2 {
    implicit object Info extends VersionInfoImpl[V2](2)
    implicit object V2IsNextVersionAfterV1 extends IsNextVersionAfter[V2, V1]
  }

  class V3 extends Version
  case object V3 extends V3 {
    implicit object Info extends VersionInfoImpl[V3](3)
    implicit object V3IsNextVersionAfterV2 extends IsNextVersionAfter[V3, V2]
  }
}

object SprayJsonPersistence {
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

    def to[HigherV <: Version: VersionInfo](migration: JsonMigration)(implicit isHigherThan: IsNextVersionAfter[HigherV, V]): JsonMigrator[HigherV] = {
      val updatedOldMigrations: Map[Int, JsonMigration] = migrations.mapValues(_ && migration)
      val newMigrations = updatedOldMigrations + (versionNumber[HigherV] -> migration)

      new JsonMigrator[HigherV](newMigrations)
    }
  }

  def from[V <: V1: VersionInfo] = new JsonMigrator[V](Map(versionNumber[V] -> JsonMigration.Identity))

  trait JsonPersister[T, V <: Version]

  // def persister[T: RootJsonFormat](key: String): JsonPersister[T, V1] = new JsonPersister[T, V1] {}
  // def persister[T: RootJsonFormat, V <: Version](key: String, migrator: JsonMigrator[V]): JsonPersister[T, V] = new JsonPersister[T, V] {}

  /*
    json deserializer(key):

    - if key matches
      - parse contents into JsValue
      - migrator(value, version) // produces a new JsValue
      - transform new JsValue into T using the RootJsonFormat

   */

  // ==========================================================================
  // Examples
  // ==========================================================================

  import spray.json.lenses.JsonLenses._
  import SprayJsonFormats._

  def bla(migration: JsonMigration) = 42
  val blam: JsonMigration = _.update('blam ! set("Blam!"))
  val blim: JsonMigration = _.update('blim ! set("Blim!"))
  bla(blam && blim)

  val migrator: JsonMigrator[V3] = from[V1].to[V2](blam).to[V3](blim)

  case class Foo(s: String, i: Int)

  // persister[Foo]("foo")

  case class Bar(b: Boolean)

  // persister[Bar, V3]("bar", )

  // persister[Bar, V3]("foo", from[V1].to[V2](renameItems)
  //                                   .to[V3](setSizeToDefault))  // produces JsonMigrator[V3]

  // trait JsonMigrator[V] {
  //   def migrate(version: Version)
  // }

  // migrator = List[(Version, JsValue) => (Version, JsValue)]

  //
  // persister -> JsonPersister[T, V]
  // migrate[A <: Version, B <: Version] -> JsonMigrator[A, B]
  //
  // JsonPersister[T, V](key) if V is V1 ->
  // JsonPersister[T, V](key, migrator[V]) if V is V1 ->
  //
  // V3 is an HList-like structure Version[V1, V2, V3]
  //
  //
  // Persister(
  //   toPersisted = {
  //     case t: T ⇒ Persisted(key, version, encoding.encode(t))
  //   },
  //   fromPersisted = {
  //     case Persisted(k, v, bytes) if k == key && v == version ⇒ encoding.decode(bytes)
  //     case Persisted(k, v, bytes) if k == key && v == version ⇒ encoding.decode(bytes)
  //   }
  // )
  //
  // We need:
  //
  //  - Version type needs to be ordered somehow or the order needs to be known
  //  - Version -> List[Int]
  //
  //

  // }

}
