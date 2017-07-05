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
   * Creates a Migrator[T, V1] that can function as a builder for
   * creating Migrator[T, V2], etc. Its migration will be the identity
   * function so calling its migrate function will not have any effect.
   */
  def from[T, V <: V1: VersionInfo]: Migrator[T, V] = new Migrator[T, V](Map(Version.numberFor[V] → identityMigration[T]), Map.empty)
}

package migrations {
  /** Runtime exception for signalling that the specified migration path is not supported. */
  case class UndefinedMigrationException(fromVersion: Int, toVersion: Int)
    extends RuntimeException(s"No migration defined from version ${fromVersion} to version ${toVersion}.")

  /**
   * A `Migrator[R, V]` can migrate raw values of type R between
   * two versions by applying all `Migration[R]`s between these versions.
   *
   * You can create instances of `Migrator[R, V]` by using
   * a small type-safe DSL consisting of two parts: the
   * `from[R, V1]` function will create a
   * `Migrator[R, V1]` and then you can use the
   * `to[V](migration: Migration[R])` function to build
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
   *  @tparam R The type of raw data being migrated. In the JSON implementation this would be `JsValue`.
   *  @tparam V The "current" version of this Migrator.
   */
  class Migrator[R, V <: Version: VersionInfo] private[stamina] (forwardMigrations: Map[Int, Migration[R]] = Map.empty, backwardMigrations: Map[Int, Migration[R]] = Map.empty) {
    def canMigrate(fromVersion: Int, toVersion: Int): Boolean = forwardMigrations.contains(fromVersion) && forwardMigrations.contains(toVersion)

    def migrate(value: R, fromVersion: Int, toVersion: Int): R = {
      def applyMigrationOn(value: R, migration: Migration[R]) = migration(value)

      if (fromVersion <= toVersion) {
        (fromVersion to toVersion).drop(1)
          .map(version ⇒ forwardMigrations.getOrElse(version, throw UndefinedMigrationException(fromVersion, toVersion)))
          .foldLeft(value)(applyMigrationOn)
      } else {
        (toVersion to fromVersion).drop(1).reverse
          .flatMap(backwardMigrations.get)
          .foldLeft(value)(applyMigrationOn)
      }
    }

    def to[NextV <: Version: VersionInfo](migration: Migration[R])(implicit isNextAfter: IsNextVersionAfter[NextV, V]) = {
      new Migrator[R, NextV](
        forwardMigrations + (Version.numberFor[NextV] → migration),
        backwardMigrations
      )
    }

    def backTo[PrevV <: Version: VersionInfo](migration: Migration[R])(implicit e: IsNextVersionAfter[V, PrevV]) = {
      new Migrator[R, V](
        forwardMigrations,
        backwardMigrations + (Version.numberFor[V] → migration)
      )
    }
  }
}
