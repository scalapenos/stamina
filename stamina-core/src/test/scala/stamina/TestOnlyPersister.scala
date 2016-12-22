package stamina

import scala.reflect._
import akka.actor._
import akka.serialization._

object TestOnlyPersister {
  private val system = ActorSystem("TestOnlyPersister")
  private val javaSerializer = new JavaSerializer(system.asInstanceOf[ExtendedActorSystem])
  import javaSerializer._

  def persister[T <: AnyRef: ClassTag](key: String): Persister[T, V1] = new JavaPersister[T](key)

  private class JavaPersister[T <: AnyRef: ClassTag](key: String) extends Persister[T, V1](key) {
    def persist(t: T): Array[Byte] = toBinary(t)
    def unpersist(manifest: Manifest, p: Array[Byte]): T = {
      if (canUnpersist(manifest)) fromBinary(p).asInstanceOf[T]
      else throw new IllegalArgumentException("")
    }
  }
}
