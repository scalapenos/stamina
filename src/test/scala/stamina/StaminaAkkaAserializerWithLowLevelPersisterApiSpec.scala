package stamina

import org.scalatest._

class StaminaAkkaAserializerWithLowLevelPersisterApiSpec extends WordSpecLike with Matchers with OptionValues with TryValues with Inside with Inspectors {
  import TestDomain._
  import json._

  val serializer = StaminaAkkaSerializer(Persister(
    toPersisted = {
      case event: ItemAdded     ⇒ Persisted("itm-add", 1, event.toJsonBytes)
      case event: ItemRemoved   ⇒ Persisted("itm-rem", 1, event.toJsonBytes)
      case event: CartCreated   ⇒ Persisted("crt-new", 1, event.toJsonBytes)
      case event: CartUpdated   ⇒ Persisted("crt-mod", 1, event.toJsonBytes)
      case event: CartDestroyed ⇒ Persisted("crt-rem", 1, event.toJsonBytes)
    },
    fromPersisted = {
      case Persisted("itm-add", 1, bytes) ⇒ bytes.fromJsonBytes[ItemAdded]
      case Persisted("itm-rem", 1, bytes) ⇒ bytes.fromJsonBytes[ItemRemoved]
      case Persisted("crt-new", 1, bytes) ⇒ bytes.fromJsonBytes[CartCreated]
      case Persisted("crt-mod", 1, bytes) ⇒ bytes.fromJsonBytes[CartUpdated]
      case Persisted("crt-rem", 1, bytes) ⇒ bytes.fromJsonBytes[CartDestroyed]
    }
  )
  )

  import serializer._

  "The StaminaAkkaSerializer, using the low-level Persister API" should {
    "correctly serialize and deserialize the current version of the domain" in {
      fromBinary(toBinary(itemAdded)) should equal(itemAdded)
      fromBinary(toBinary(itemRemoved)) should equal(itemRemoved)
      fromBinary(toBinary(cartCreated)) should equal(cartCreated)
      fromBinary(toBinary(cartUpdated)) should equal(cartUpdated)
      fromBinary(toBinary(cartDestroyed)) should equal(cartDestroyed)
    }

    "throw an UnregistredTypeException when serializing an unregistered type" in {
      a[UnregistredTypeException] should be thrownBy toBinary("a raw String is not supported")
    }

    "throw an UnregisteredKeyException when deserializing a Persisted with an unregistered key" in {
      an[UnregisteredKeyException] should be thrownBy fromBinary(StaminaAkkaSerializer.toBinary(Persisted("unregistered", ByteString("unregistered"))))
    }

    "throw an UnrecoverableDataException when an exception occurs while deserializing" in {
      an[UnrecoverableDataException] should be thrownBy fromBinary(StaminaAkkaSerializer.toBinary(Persisted("crt-new", ByteString("unregistered"))))
    }

    // This is very slow so only un-ignore when you need to compare serializer implementations
    "be very fast when run in a micro-benchmark" ignore {
      def microBench(iterations: Int): Long = {
        val before = System.currentTimeMillis
        List.fill(iterations)(toBinary(cartCreated)).map(fromBinary(_).asInstanceOf[CartCreated])
        System.currentTimeMillis - before
      }

      val timings = List.fill(10)(microBench(1000000))

      println(s"====================> Json benchmarks: min = ${timings.min}, max = ${timings.max}")
    }

    // This just produces sizing statistics so only un-ignore when you need to compare serializer implementations
    "produce compact byte representations" ignore {
      println(s"====================> Json size: ${toBinary(cartCreated).length} bytes")
    }
  }
}
