package stamina

class PersistersSpec extends StaminaSpec {
  import TestDomain._
  import TestOnlyPersister._

  val itemPersister = persister[Item]("item")
  val cartPersister = persister[Cart]("cart")
  val cartCreatedPersister = persister[CartCreated]("cart-created")

  val persisters = Persisters(itemPersister, cartPersister)

  "A non-empty instance of Persisters" should {
    import persisters._

    "correctly implement canPersist()" in {
      canPersist(item1) should be(true)
      canPersist(cart) should be(true)

      canPersist(cartCreated) should be(false)
      canPersist("Huh?") should be(false)
    }

    "correctly implement canUnpersist()" in {
      canUnpersist(itemPersister.currentManifest) should be(true)
      canUnpersist(cartPersister.currentManifest) should be(true)

      canUnpersist(cartCreatedPersister.currentManifest) should be(false)
      canUnpersist(Manifest("unknown", 1)) should be(false)
      canUnpersist(Manifest("item", 2)) should be(false)
    }

    "correctly implement persist() and unpersist()" in {
      unpersist(persist(item1)) should equal(item1)
      unpersist(persist(cart)) should equal(cart)
    }

    "throw an UnregisteredTypeException when persisting an unregistered type" in {
      a[UnregisteredTypeException] should be thrownBy persist("a raw String is not supported")
    }

    "throw an UnsupportedDataException when unpersisting data with an unknown key" in {
      an[UnsupportedDataException] should
        be thrownBy unpersist(Array[Byte](), Manifest("unknown", 1))
    }

    "throw an UnsupportedDataException when deserializing data with an unsupported version" in {
      an[UnsupportedDataException] should
        be thrownBy unpersist(Array[Byte](), Manifest("item", 2))
    }

    "throw an UnrecoverableDataException when an exception occurs while deserializing" in {
      an[UnrecoverableDataException] should
        be thrownBy unpersist(ByteString("not an item").toArray, itemPersister.currentManifest)
    }
  }
}
