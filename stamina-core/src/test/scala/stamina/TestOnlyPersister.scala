package stamina

import akka.actor._
import akka.serialization._
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

object TestOnlyPersister {
  private val system = ActorSystem("TestOnlyPersister")
  private val javaSerializer = new JavaSerializer(system.asInstanceOf[ExtendedActorSystem])
  import javaSerializer._

  def persister[T <: AnyRef: ClassTag: TypeTag](key: String): Persister[T, V1] = new JavaPersister[T](key)

  private class JavaPersister[T <: AnyRef: ClassTag: TypeTag](key: String) extends Persister[T, V1](key) {
    def persist(t: T): Persisted = Persisted(key, currentVersion, toBinary(t))
    def unpersist(p: Persisted): T = {
      if (canUnpersist(p)) fromBinary(p.bytes.toArray).asInstanceOf[T]
      else throw new IllegalArgumentException("")
    }
  }
}
