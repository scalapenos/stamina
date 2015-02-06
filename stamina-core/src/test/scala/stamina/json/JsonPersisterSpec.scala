package stamina
package json

class JsonPersisterSpec extends StaminaSpec {
  import JsonTestDomain._
  import SprayJsonFormats._
  import spray.json.lenses.JsonLenses._

  val v1CartCreatedPersister = persister[CartCreatedV1]("cart-created")

  val v2CartCreatedPersister = persister[CartCreatedV2, V2]("cart-created",
    from[V1].to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
  )

  val v3CartCreatedPersister = persister[CartCreatedV3, V3]("cart-created",
    from[V1]
      .to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
      .to[V3](_.update('timestamp ! set[Long](System.currentTimeMillis - 3600000L)))
  )

  "V1 persisters produced by SprayJsonPersister" should {
    "correctly persist and unpersist domain events " in {
      import v1CartCreatedPersister._
      unpersist(persist(v1CartCreated)) should equal(v1CartCreated)
    }
  }

  "V2 persisters with migrators produced by SprayJsonPersister" should {
    "correctly persist and unpersist domain events " in {
      import v2CartCreatedPersister._
      unpersist(persist(v2CartCreated)) should equal(v2CartCreated)
    }

    "correctly migrate and unpersist V1 domain events" in {
      val v1Persisted = v1CartCreatedPersister.persist(v1CartCreated)
      val v2Unpersisted = v2CartCreatedPersister.unpersist(v1Persisted)

      v2Unpersisted.cart.items.map(_.price).toSet should equal(Set(1000))
    }
  }

  "V3 persisters with migrators produced by SprayJsonPersister" should {
    "correctly persist and unpersist domain events " in {
      import v3CartCreatedPersister._
      unpersist(persist(v3CartCreated)) should equal(v3CartCreated)
    }

    "correctly migrate and unpersist V1 domain events" in {
      val v1Persisted = v1CartCreatedPersister.persist(v1CartCreated)
      val v2Persisted = v2CartCreatedPersister.persist(v2CartCreated)

      val v1Unpersisted = v3CartCreatedPersister.unpersist(v1Persisted)
      val v2Unpersisted = v3CartCreatedPersister.unpersist(v2Persisted)

      v1Unpersisted.cart.items.map(_.price).toSet should equal(Set(1000))
      v2Unpersisted.timestamp should (be > 0L and be < System.currentTimeMillis)
    }
  }
}
