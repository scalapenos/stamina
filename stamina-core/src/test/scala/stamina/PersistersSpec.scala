package stamina

class PersistersSpec extends StaminaSpec {
  import TestDomain._
  import TestOnlyPersister._

  val itemPersister = persister[Item]("item")
  val cartPersister = persister[Cart]("cart")

  val persisters = Persisters(itemPersister, cartPersister)

  val cartCreatedPersister = persister[CartCreated]("cart-created")

  "An non-empty instance of Persisters" should {
    import persisters._

    "correctly implement canPersist()" in {
      canPersist(item1) should be(true)
      canPersist(cart) should be(true)

      canPersist(cartCreated) should be(false)
      canPersist("Huh?") should be(false)
    }

    "correctly implement canUnpersist()" in {
      canUnpersist(itemPersister.persist(item1)) should be(true)
      canUnpersist(cartPersister.persist(cart)) should be(true)

      canUnpersist(cartCreatedPersister.persist(cartCreated)) should be(false)

      // works because canUnpersist only looks at the key and the version, not at the raw data
      canUnpersist(Persisted("item", 1, ByteString("Not an item at all!"))) should be(true)
    }

    "correctly implement persist() and unpersist()" in {
      unpersist(persist(item1)) should equal(item1)
      unpersist(persist(cart)) should equal(cart)
    }

    "correctly signal errors from persist() and unpersist()" in {
      an[UnregistredTypeException] should be thrownBy persist("I don't think so...")
      an[UnsupportedDataException] should be thrownBy unpersist(cartCreatedPersister.persist(cartCreated))
      an[UnrecoverableDataException] should be thrownBy unpersist(Persisted("item", 1, ByteString("Not an item at all!")))
    }
  }
}
