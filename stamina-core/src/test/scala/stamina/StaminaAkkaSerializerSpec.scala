package stamina

class StaminaAkkaSerializerSpec extends StaminaSpec {
  import TestDomain._
  import TestOnlyPersister._
  import DefaultPersistedCodec._

  val itemPersister = persister[Item]("item")
  val cartPersister = persister[Cart]("cart")
  val cartCreatedPersister = persister[CartCreated]("cart-created")

  class MyAkkaSerializer1a extends StaminaAkkaSerializer(List(itemPersister, cartPersister, cartCreatedPersister))
  class MyAkkaSerializer1b extends StaminaAkkaSerializer(List(itemPersister, cartPersister, cartCreatedPersister), DefaultPersistedCodec)
  class MyAkkaSerializer2 extends StaminaAkkaSerializer(itemPersister, cartPersister, cartCreatedPersister)

  val serializer = new MyAkkaSerializer1a

  import serializer._

  "The StaminaAkkaSerializer" should {
    "correctly serialize and deserialize the current version of the domain" in {
      fromBinary(toBinary(item1)) should equal(item1)
      fromBinary(toBinary(item2)) should equal(item2)
      fromBinary(toBinary(cart)) should equal(cart)
      fromBinary(toBinary(cartCreated)) should equal(cartCreated)
    }

    "throw an UnregisteredTypeException when serializing an unregistered type" in {
      a[UnregisteredTypeException] should be thrownBy toBinary("a raw String is not supported")
    }

    "throw an UnsupportedDataException when deserializing data with an unknown key" in {
      an[UnsupportedDataException] should
        be thrownBy fromBinary(writePersisted(Persisted("unknown", 1, ByteString("..."))))
    }

    "throw an UnsupportedDataException when deserializing data with an unsupported version" in {
      an[UnsupportedDataException] should
        be thrownBy fromBinary(writePersisted(Persisted("item", 2, ByteString("..."))))
    }

    "throw an UnrecoverableDataException when an exception occurs while deserializing" in {
      an[UnrecoverableDataException] should
        be thrownBy fromBinary(writePersisted(Persisted("item", 1, ByteString("not an item"))))
    }
  }
}
