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
    "correctly implement canPersist()" in {
      persisters.canPersist(v1Item1) should be(true)
      persisters.canPersist(v1Cart) should be(true)
      persisters.canPersist(v1CartCreated) should be(true)

      persisters.canPersist(v2Item1) should be(false)
      persisters.canPersist(v2Cart) should be(false)
      persisters.canPersist(v2CartCreated) should be(false)
    }

    "correctly implement canUnpersist()" in {
      persisters.canUnpersist(itemPersister.persist(v1Item1)) should be(true)
      persisters.canUnpersist(cartPersister.persist(v1Cart)) should be(true)
      persisters.canUnpersist(cartCreatedPersister.persist(v1CartCreated)) should be(true)

      persisters.canUnpersist(v2CartCreatedPersister.persist(v2CartCreated)) should be(false)
    }
  }
}
