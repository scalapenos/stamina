package object stamina {
  /**
   * Marker trait for event classes that need to be persisted using Akka Persistence.
   * Used by the Akka serialization config to indicate which serializer to use for
   * persistent events.
   */
  trait AkkaSerializable extends java.io.Serializable

  /**  */
  case class Serialized(key: String, bytes: ByteString)

  /**  */
  type Serializers = PartialFunction[AnyRef, Serialized]
  /**  */
  type Deserializers = PartialFunction[Serialized, AnyRef]

  /**  */
  case class Serialization(serializers: Serializers, deserializers: Deserializers) {
    def orElse(other: Serialization) = Serialization(
      this.serializers orElse other.serializers,
      this.deserializers orElse other.deserializers
    )
  }

  /**  */
  object Serialization {
    def compose(first: Serialization, rest: Serialization*): Serialization = {
      rest.foldLeft(first)((accumulator, entry) ⇒ accumulator orElse entry)
    }
  }


  /**  */
  type ByteString = akka.util.ByteString
  val ByteString = akka.util.ByteString
}
