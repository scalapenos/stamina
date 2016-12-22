package stamina

import scala.reflect._
import scala.util._
import scala.reflect.runtime.universe.{Try ⇒ _, _}

/**
 * A Persister[T, V] provides a type-safe API for persisting instances of T
 * at version V and unpersisting persisted instances of T for all versions up
 * to and including version V.
 */
abstract class Persister[T: ClassTag: TypeTag, V <: Version: VersionInfo](val key: String) {
  lazy val currentVersion = Version.numberFor[V]

  def persist(t: T): Persisted
  def unpersist(persisted: Persisted): T

  def canPersist(a: AnyRef): Boolean = convertToT(a).isDefined
  def canUnpersist(p: Persisted): Boolean = p.key == key && p.version <= currentVersion

  private[stamina] def convertToT(any: AnyRef): Option[T] = any match {
    case t: T ⇒ t match {
      case tagged: TypeTagged[_] ⇒
        if (typeTag[T].tpe =:= tagged.tag.tpe) Some(t)
        else None
      case _ ⇒
        Some(t)
    }
    case _ ⇒ None
  }

  private[stamina] def persistAny(any: AnyRef): Persisted = {
    convertToT(any).map(persist(_)).getOrElse(
      throw new IllegalArgumentException(
        s"persistAny() was called on Persister[${implicitly[ClassTag[T]].runtimeClass}] with an instance of ${any.getClass}."
      )
    )
  }

  private[stamina] def unpersistAny(persisted: Persisted): AnyRef = {
    Try(unpersist(persisted).asInstanceOf[AnyRef]) match {
      case Success(anyref) ⇒ anyref
      case Failure(error)  ⇒ throw UnrecoverableDataException(persisted, error)
    }
  }

  private[stamina] val tag =
    if (typeTag[T].tpe <:< typeOf[TypeTagged[_]]) typeTag[T].tpe
    else classTag[T].runtimeClass
}

object Persister {
  implicit def optionTypeTag[E](implicit typeTag: TypeTag[E]) = Some(typeTag)
}
