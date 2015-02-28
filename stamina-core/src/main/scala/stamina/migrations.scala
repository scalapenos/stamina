package stamina

package object migrations {
  /**
   * A Migration[T] is a simple type alias for a function that takes a T
   * and produces another T, usually the input value transformed in some way
   * in order to make it compatible with a higher version.
   */
  type Migration[T] = T ⇒ T

  /** The identity Migration will always return its input as its output. */
  def identityMigration[T]: Migration[T] = identity[T]

  /**
   * Adds support for combining two instances of Migration[T] into a new
   * Migration[T] that will apply the first one and then the second one.
   */
  implicit class MigrationWithComposition[T](val firstMigration: Migration[T]) extends AnyVal {
    def &&(secondMigration: Migration[T]): Migration[T] = {
      (value: T) ⇒ secondMigration(firstMigration(value))
    }
  }

  /**
   * Creates a Migrator[A, V1] that can function as a builder for
   * creating Migrator[A, V2], etc. Its migration will be the identity
   * function so calling its migrate function will not have any effect.
   */
  def from[A, V <: V1: VersionInfo]: Migrator[A, V] = new Migrator[A, V](Map(Version.numberFor[V] -> identityMigration[A]))
}

package migrations {
  /** Runtime exception for signalling that the specified migration path is not supported. */
  case class UndefinedMigrationException(fromVersion: Int, toVersion: Int)
    extends RuntimeException(s"No migration defined from version ${fromVersion} to version ${toVersion}.")

  /**
   * A `Migrator[A, V]` can migrate values from older
   * versions to version `V` by applying a specific,
   * usually composed `Migration[A]` to it.
   *
   * You can create instances of `Migrator[A, V]` by using
   * a small type-safe DSL consisting of two parts: the
   * `from[A, V1]` function will create a
   * `Migrator[A, V1]` and then you can use the
   * `to[V](migration: Migration[A])` function to build
   * instances that can migrate multiple versions.
   *
   * @example Using the json implementation:
   * {{{
   * val p = persister[CartCreated, V3]("cart-created",
   *   from[JsValue, V1]
   *     .to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
   *     .to[V3](_.update('timestamp ! set[Long](System.currentTimeMillis - 3600000L)))
   * )
   * }}}
   *
   *  @tparam A The type of raw data being migrated. In the JSON implementation this would be `JsValue`.
   *  @tparam V The "current" version of this Migrator, i.e. it can migrate values from V1 to this version or any version in between.
   */
  class Migrator[A, V <: Version: VersionInfo] private[stamina] (migrations: Map[Int, Migration[A]] = Map.empty) {
    def canMigrate(fromVersion: Int): Boolean = migrations.contains(fromVersion)

    def migrate(value: A, fromVersion: Int): A = {
      migrations.get(fromVersion).map(_.apply(value)).getOrElse(
        throw UndefinedMigrationException(fromVersion, Version.numberFor[V])
      )
    }

    def to[NextV <: Version: VersionInfo](migration: Migration[A])(implicit isNextAfter: IsNextAfter[NextV, V]) = {
      new Migrator[A, NextV](
        migrations.mapValues(_ && migration) + (Version.numberFor[NextV] -> identityMigration[A])
      )
    }
  }
}
