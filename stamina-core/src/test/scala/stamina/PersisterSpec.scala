package stamina

class PersisterSpec extends StaminaSpec {
  import TestOnlyPersister._
  import TestDomain._

  "A Persister" should {
    "correctly report whether it can persist a given class, even after translation" in {
      val cartPersister = persister[CartCreated]("cartCreated")
      cartPersister.canPersist(cartCreated) should be(true)
      cartPersister.canPersist(item1) should be(false)

      val translatedCartPersister = cartPersister.translate(_.toString, (s: String) ⇒ s.getBytes)
      translatedCartPersister.canPersist(cartCreated) should be(true)
      translatedCartPersister.canPersist(item1) should be(false)
    }
  }
}
