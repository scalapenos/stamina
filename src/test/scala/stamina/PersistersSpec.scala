package stamina

class PersistersSpec extends StaminaSpec {
  import TestDomain._
  import json._
  import json.SprayJsonFormats._

  val itemPersister = persister[Item]("item")
  val cartPersister = persister[Cart]("cart")
  val cartCreatedPersister = persister[CartCreated]("cart-created")

  val persisters = Persisters(itemPersister, cartPersister, cartCreatedPersister)

  "An non-empty instance of Persisters" should {
    "correctly implement canPersist()" in {
      persisters.canPersist(item1) should be(true)
      persisters.canPersist(cart) should be(true)
      persisters.canPersist(cartCreated) should be(true)

      persisters.canPersist(v2Item1) should be(false)
      persisters.canPersist(v2Cart) should be(false)
      persisters.canPersist(v2CartCreated) should be(false)
    }

    "correctly implement canUnpersist()" in {
      persisters.canUnpersist(itemPersister.persist(item1)) should be(true)
      persisters.canUnpersist(cartPersister.persist(cart)) should be(true)
      persisters.canUnpersist(cartCreatedPersister.persist(cartCreated)) should be(true)

      // persisters.canUnpersist(v2CartCreatedPersister.persist(v2CartCreated)) should be(false)
    }
  }
}
