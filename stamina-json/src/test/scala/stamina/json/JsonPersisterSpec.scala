package stamina
package json

class JsonPersisterSpec extends StaminaJsonSpec {

  import JsonTestDomain._
  import fommil.sjs.FamilyFormats._
  import spray.json.lenses.JsonLenses._

  val v1CartCreatedPersister = persister[CartCreatedV1]("cart-created")

  val v1CartCreatedPersisterWithBackwardMigration = persister[CartCreatedV1](
    "cart-created",
    from[V1].backFrom[V2](identity)
  )

  val v2CartCreatedPersister = persister[CartCreatedV2, V2](
    "cart-created",
    from[V1].to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
  )

  val migratorV3 =
    from[V1]
      .to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
      .to[V3](_.update('timestamp ! set[Long](System.currentTimeMillis - 3600000L)))

  val v3CartCreatedPersister = persister[CartCreatedV3, V3](
    "cart-created",
    migratorV3
  )

  val v3CartCreatedPersisterWithBackwardMigration = persister[CartCreatedV3, V3](
    "cart-created",
    migratorV3
      .backFrom[V4](_.update(('cart / 'items / * / 'name.?) ! setOrUpdateField[String]("unknown")(identity)))
  )

  val v4SimpleCartCreatedPersister = persister[CartCreatedV4, V4](
    "cart-created",
    from[V1].to[V2](identity).to[V3](identity).to[V4](identity)
  )

  "V1 persisters produced by SprayJsonPersister" should {
    "correctly persist and unpersist domain events " in {
      import v1CartCreatedPersister._
      unpersist(persist(v1CartCreated)) should equal(v1CartCreated)
    }

    "fail to unpersist V2 domain events" in {
      val v2Persisted = v2CartCreatedPersister.persist(v2CartCreated)
      val e = intercept[IllegalArgumentException](v1CartCreatedPersister.unpersist(v2Persisted))
      e.getMessage.contains("cannot unpersist data") shouldBe true
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

    "fail to migrate and unpersist V4 domain events" in {
      val v4Persisted = v4SimpleCartCreatedPersister.persist(v4CartCreated)
      val e = intercept[IllegalArgumentException](v3CartCreatedPersister.unpersist(v4Persisted))
      e.getMessage.contains("cannot unpersist data") shouldBe true
    }
  }

  "V1 persisters with migrators with backward migrations produced by SprayJsonPersister" should {
    "correctly persist and unpersist domain events" in {
      import v1CartCreatedPersisterWithBackwardMigration._
      unpersist(persist(v1CartCreated)) should equal(v1CartCreated)
    }

    "correctly migrte and unpersist V2 domain events" in {
      val v2Persister = v2CartCreatedPersister.persist(v2CartCreated)
      val v1UnpersistedFromV2 = v1CartCreatedPersisterWithBackwardMigration.unpersist(v2Persister)
      v1UnpersistedFromV2 should equal(v1CartCreated)
    }
  }

  "V4 dummy persister" should {
    "correctly persist and unpersist domain events " in {
      import v4SimpleCartCreatedPersister._
      unpersist(persist(v4CartCreated)) should equal(v4CartCreated)
    }
  }

  "V3 persisters with backward migrations and migrators produced by SprayJsonPersister" should {
    "correctly persist and unpersist domain events " in {
      import v3CartCreatedPersister._
      unpersist(persist(v3CartCreated)) should equal(v3CartCreated)
    }

    "correctly migrate and unpersist V1 domain events" in {
      val v1Persisted = v1CartCreatedPersister.persist(v1CartCreated)
      val v2Persisted = v2CartCreatedPersister.persist(v2CartCreated)

      val v1Unpersisted = v3CartCreatedPersisterWithBackwardMigration.unpersist(v1Persisted)
      val v2Unpersisted = v3CartCreatedPersisterWithBackwardMigration.unpersist(v2Persisted)

      v1Unpersisted.cart.items.map(_.price).toSet should equal(Set(1000))
      v2Unpersisted.timestamp should (be > 0L and be < System.currentTimeMillis)
    }

    "correctly migrate and unpersist V4 domain events setting default value for removed field" in {
      val v4Persisted = v4SimpleCartCreatedPersister.persist(v4CartCreated)

      val v3UnpersistedFromV4 = v3CartCreatedPersisterWithBackwardMigration.unpersist(v4Persisted)

      v3UnpersistedFromV4.cart.items.map(_.name) should equal(List("Wonka Bar", "unknown"))
    }
  }
}
