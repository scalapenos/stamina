package stamina
import scala.reflect.runtime.universe._

case class PayloadEvent[E: TypeTag](payload: E) extends TypeTagged[PayloadEvent[E]]
case class Payload1(txt: String)
case class Payload2(value: Int)
case object Payload3
case class Payload4[T](list: List[T])

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
      canUnpersist(itemPersister.persist(item1)) should be(true)
      canUnpersist(cartPersister.persist(cart)) should be(true)

      canUnpersist(cartCreatedPersister.persist(cartCreated)) should be(false)
      canUnpersist(Persisted("unknown", 1, ByteString("..."))) should be(false)
      canUnpersist(Persisted("item", 2, ByteString("..."))) should be(false)

      // works because canUnpersist only looks at the key and the version, not at the raw data
      canUnpersist(Persisted("item", 1, ByteString("Not an item at all!"))) should be(true)
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
        be thrownBy unpersist(Persisted("unknown", 1, ByteString("...")))
    }

    "throw an UnsupportedDataException when deserializing data with an unsupported version" in {
      an[UnsupportedDataException] should
        be thrownBy unpersist(Persisted("item", 2, ByteString("...")))
    }

    "throw an UnrecoverableDataException when an exception occurs while deserializing" in {
      an[UnrecoverableDataException] should
        be thrownBy unpersist(Persisted("item", 1, ByteString("not an item")))
    }
  }

  "Persist overlapping events using the TypeTagged marker interface" should {
    val persister1 = persister[PayloadEvent[Payload1]]("payload1")
    val persister2 = persister[PayloadEvent[Payload2]]("payload2")
    val persister4 = persister[PayloadEvent[Payload4[_]]]("payload4")

    val event1 = PayloadEvent(Payload1("test"))
    val event2 = PayloadEvent(Payload2(123))
    val event3 = PayloadEvent(Payload3)
    val event4a = PayloadEvent(Payload4(List(1, 2, 3)))
    val event4b = PayloadEvent(Payload4(List("a", "b", "c")))

    val nestedPersisters = Persisters(persister1, persister2, persister4)
    import nestedPersisters._

    "Persist nested events correctly" in {
      canPersist(event1) should be(true)
      canPersist(event2) should be(true)
      canPersist(event3) should be(false)

      // We currently don't support tagged types with abstract parameters:
      canPersist(event4a) should be(false)
      canPersist(event4b) should be(false)
    }

    "correctly implement canUnpersist()" in {
      canUnpersist(persister1.persist(event1)) should be(true)
      canUnpersist(persister2.persist(event2)) should be(true)
    }
  }
}
