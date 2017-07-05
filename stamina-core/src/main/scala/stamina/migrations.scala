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
   * Creates a Migrator[T, V1] that can function as a builder for
   * creating Migrator[T, V2], etc. Its migration will be the identity
   * function so calling its migrate function will not have any effect.
   */
  def from[T, V <: V1: VersionInfo]: Migrator[T, V] = new Migrator[T, V](Map(Version.numberFor[V] → identityMigration[T]), None)
}

package migrations {
  /** Runtime exception for signalling that the specified migration path is not supported. */
  case class UndefinedMigrationException(fromVersion: Int, toVersion: Int)
    extends RuntimeException(s"No migration defined from version ${fromVersion} to version ${toVersion}.")

  /**
   * A `Migrator[R, V]` can migrate raw values of type R from older
   * versions to version `V` or from version one generation younger
   * than `V` back to `V` by applying a specific `Migration[R]` to it.
   *
   * You can create instances of `Migrator[R, V]` by using
   * a small type-safe DSL consisting of two parts: the
   * `from[R, V1]` function will create a
   * `Migrator[R, V1]` and then you can use the
   * `to[V](migration: Migration[R])` function to build
   * instances that can migrate multiple versions.
   * `backFrom[V]` can be used to define migration from version
   * one generation younger than current `Migrator` version.
   * This functionality can be useful when you want to do
   * e.g. rolling update in clustered application - first
   * deploy to all nodes app that still persists events with current
   * version but is able to read also newer events, then deploy
   * app that actually saves data in new format.
   * Note that `backFrom[V]` is effective only when
   * it is called as the last one in call chain when defining
   * `Migrator[R, V]`
   *
   * @example Using the json implementation:
   * {{{
   * val p = persister[CartCreated, V3]("cart-created",
   *   from[JsValue, V1]
   *     .to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
   *     .to[V3](_.update('timestamp ! set[Long](System.currentTimeMillis - 3600000L)))
   *     .backFrom[V4](_.update('cart / 'items / * / 'name ! set[String]("unknown")))
   * )
   * }}}
   *
   *  @tparam R The type of raw data being migrated. In the JSON implementation this would be `JsValue`.
   *  @tparam V The "current" version of this Migrator, i.e. it can migrate values from V1 to this version or any version in between
    *            and optionally from next version back to this one.
   */
  class Migrator[R, V <: Version : VersionInfo] private[stamina](migrations: Map[Int, Migration[R]] = Map.empty, backwardMigration: Option[Migration[R]] = None) {
    def canMigrate(fromVersion: Int): Boolean = migrations.contains(fromVersion) || (backwardMigration.isDefined && fromVersion == Version.numberFor[V] + 1)

    def migrate(value: R, fromVersion: Int): R = {
      val thisVersion = Version.numberFor[V]
      if (fromVersion <= thisVersion) {
        migrations.get(fromVersion).map(_.apply(value)).getOrElse(
          throw UndefinedMigrationException(fromVersion, thisVersion)
        )
      } else if (fromVersion == thisVersion + 1) {
        backwardMigration.map(_.apply(value)).getOrElse(
          throw UndefinedMigrationException(fromVersion, thisVersion)
        )
      } else {
        throw UndefinedMigrationException(fromVersion, thisVersion)
      }
    }

    def to[NextV <: Version : VersionInfo](migration: Migration[R])(implicit isNextAfter: IsNextVersionAfter[NextV, V]): Migrator[R, NextV] = {
      new Migrator[R, NextV](
        migrations.mapValues(_ && migration) + (Version.numberFor[NextV] → identityMigration[R]),
        None
      )
    }

    def backFrom[NextV <: Version : VersionInfo](migration: Migration[R])(implicit isNextAfter: IsNextVersionAfter[NextV, V]): Migrator[R, V] = {
      new Migrator[R, V](
        migrations,
        Some(migration)
      )
    }
  }

}
