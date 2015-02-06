package stamina

class StaminaAkkaSerializerSpec extends StaminaSpec {
  import TestDomain._

  // class TestSerializer extends StaminaAkkaSerializer(Persister(
  //   toPersisted = {
  //     case event: CartCreated ⇒ Persisted("crt-new", 1, event.toJsonBytes)
  //   },
  //   fromPersisted = {
  //     case Persisted("crt-new", 1, bytes) ⇒ bytes.fromJsonBytes[CartCreated]
  //   }
  // ))

  // val serializer = new TestSerializer()

  // import serializer._

  // "The StaminaAkkaSerializer, using the low-level Persister API" should {
  //   "correctly serialize and deserialize the current version of the domain" in {
  //     fromBinary(toBinary(cartCreated)) should equal(cartCreated)
  //   }

  //   "throw an UnregistredTypeException when serializing an unregistered type" in {
  //     a[UnregistredTypeException] should be thrownBy toBinary("a raw String is not supported")
  //   }

  //   "throw an UnregisteredKeyException when deserializing a Persisted with an unregistered key" in {
  //     an[UnregisteredKeyException] should be thrownBy fromBinary(StaminaAkkaSerializer.toBinary(Persisted("unregistered", 1, ByteString("unregistered"))))
  //   }

  //   "throw an UnrecoverableDataException when an exception occurs while deserializing" in {
  //     an[UnrecoverableDataException] should be thrownBy fromBinary(StaminaAkkaSerializer.toBinary(Persisted("crt-new", 1, ByteString("unregistered"))))
  //   }
}
