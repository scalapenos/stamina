package stamina

import scala.reflect.ClassTag

/**
 * Persisters is the bridge between the type-safe world of <code>Persister[T, V]</code>
 * and the untyped, AnyRef world of Akka serializers. It wraps one or more
 * instances of <code>Persister[_, _]</code> and combines them together to form
 * one single entry-point for subclasses of <code>StaminaAkkaSerializer</code>
 *
 */
case class Persisters(persisters: List[Persister[_, _]]) {
  requireNoOverlappingTags()

  def canPersist(a: AnyRef): Boolean = persisters.exists(_.canPersist(a))
  def canUnpersist(p: Persisted): Boolean = persisters.exists(_.canUnpersist(p))

  // format: OFF
  def persist(anyref: AnyRef): Persisted = {
    persisters.find(_.canPersist(anyref))
              .map(_.persistAny(anyref))
              .getOrElse(throw UnregisteredTypeException(anyref))
  }

  def unpersist(persisted: Persisted): AnyRef = {
    persisters.find(_.canUnpersist(persisted))
              .map(_.unpersistAny(persisted))
              .getOrElse(throw UnsupportedDataException(persisted))
  }
  // format: ON

  def ++(other: Persisters): Persisters = Persisters(persisters ++ other.persisters)

  private def requireNoOverlappingTags() = {
    val overlappingTags: Map[ClassTag[_], Seq[String]] =
      persisters
        .groupBy[ClassTag[_]](_.tag)
        .collect {
          case (tag, persisters) if persisters.length > 1 =>
            (tag, persisters.map(_.key))
        }.toMap

    val warnings = overlappingTags.map {
      case (tag, keys) =>
        s"""Persisters with keys ${keys.mkString("'", "', '", "'")} all persist ${tag.runtimeClass}."""
    }

    require(overlappingTags.isEmpty, s"""Overlapping persisters: ${warnings.mkString(" ")}""")
  }
}

object Persisters {
  def apply[T: ClassTag, V <: Version: VersionInfo](persister: Persister[T, V]): Persisters = apply(List(persister))
  def apply(first: Persister[_, _], rest: Persister[_, _]*): Persisters = apply(first :: rest.toList)
}
