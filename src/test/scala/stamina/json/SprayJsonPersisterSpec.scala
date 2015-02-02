package stamina
package json

import org.scalatest._

class SprayJsonPersistenceSpec extends WordSpecLike with Matchers with OptionValues with TryValues with Inside with Inspectors {
  import TestDomain._
  import SprayJsonFormats._
  import SprayJsonPersistence._
  import spray.json.lenses.JsonLenses._

  "V1 persisters produced by SprayJsonPersister" should {
    "correctly persist and unpersist domain events " in {
      import v1CartCreatedPersister._
      unpersist(persist(cartCreated)) should equal(cartCreated)
    }
  }

  "V2 persisters with migrators produced by SprayJsonPersister" should {
    "correctly persist and unpersist domain events " in {
      import v2CartCreatedPersister._
      unpersist(persist(v2CartCreated)) should equal(v2CartCreated)
    }

    "correctly migrate and unpersist V1 domain events" in {
      val v1Persisted = v1CartCreatedPersister.persist(cartCreated)
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
      val v1Persisted = v1CartCreatedPersister.persist(cartCreated)
      val v2Persisted = v2CartCreatedPersister.persist(v2CartCreated)

      val v1Unpersisted = v3CartCreatedPersister.unpersist(v1Persisted)
      val v2Unpersisted = v3CartCreatedPersister.unpersist(v2Persisted)

      v1Unpersisted.cart.items.map(_.price).toSet should equal(Set(1000))
      v2Unpersisted.timestamp should (be > 0L and be < System.currentTimeMillis)
    }
  }
}
