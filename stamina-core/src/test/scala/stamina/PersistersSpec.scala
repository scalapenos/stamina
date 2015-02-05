package stamina

class PersistersSpec extends StaminaSpec {
  import TestDomain._
  import json._
  import json.SprayJsonFormats._
  import spray.json.lenses.JsonLenses._

  val itemPersister = persister[ItemV1]("item")
  val cartPersister = persister[CartV1]("cart")
  val cartCreatedPersister = persister[CartCreatedV1]("cart-created")

  val persisters = Persisters(itemPersister, cartPersister, cartCreatedPersister)

  val v2CartCreatedPersister = persister[CartCreatedV2, V2]("cart-created",
    from[V1].to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
  )

  "An non-empty instance of Persisters" should {
    import persisters._

    "correctly implement canPersist()" in {
      canPersist(v1Item1) should be(true)
      canPersist(v1Cart) should be(true)
      canPersist(v1CartCreated) should be(true)

      canPersist(v2Item1) should be(false)
      canPersist(v2Cart) should be(false)
      canPersist(v2CartCreated) should be(false)
      canPersist("Huh?") should be(false)
    }

    "correctly implement canUnpersist()" in {
      canUnpersist(itemPersister.persist(v1Item1)) should be(true)
      canUnpersist(cartPersister.persist(v1Cart)) should be(true)
      canUnpersist(cartCreatedPersister.persist(v1CartCreated)) should be(true)

      canUnpersist(v2CartCreatedPersister.persist(v2CartCreated)) should be(false)

      // works because canUnpersist only looks at the key and the version, not at the raw data
      canUnpersist(Persisted("item", 1, ByteString("Not an item at all!"))) should be(true)
    }

    "correctly implement persist() and unpersist()" in {
      unpersist(persist(v1Item1)) should equal(v1Item1)
      unpersist(persist(v1Cart)) should equal(v1Cart)
      unpersist(persist(v1CartCreated)) should equal(v1CartCreated)
    }

    "correctly signal errors from persist() and unpersist()" in {
      an[UnregistredTypeException] should be thrownBy persist("I don't think so...")
      an[UnsupportedDataException] should be thrownBy unpersist(v2CartCreatedPersister.persist(v2CartCreated))
      an[UnrecoverableDataException] should be thrownBy unpersist(Persisted("item", 1, ByteString("Not an item at all!")))
    }
  }
}
