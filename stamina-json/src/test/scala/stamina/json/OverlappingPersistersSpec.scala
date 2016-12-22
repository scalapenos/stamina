package stamina
package json

import spray.json._
import DefaultJsonProtocol._

class OverlappingPersisterSpec extends StaminaJsonSpec {
  import OverlappingPersisterSpecDomain._

  implicit val payload1Format = jsonFormat1(Payload1)
  implicit val payload2Format = jsonFormat1(Payload2)

  implicit val eventPayload1Format = jsonFormat1(Event[Payload1])
  implicit val eventPayload2Format = jsonFormat1(Event[Payload2])

  "The persisters construction DSL" should {

    /** #43 In the future we might want to support this situation instead of failing at initialization time */
    "correctly handle overlapping persisters" in {
      val e = intercept[IllegalArgumentException] {
        Persisters(
          persister[Event[Payload1]]("payload1"),
          persister[Event[Payload2]]("payload2")
        )
      }
      val className = s"stamina.json.OverlappingPersisterSpecDomain$$Event"
      e.getMessage() should be(s"requirement failed: Overlapping persisters: Persisters with keys 'payload1', 'payload2' all persist class $className.")

      /**
       * When we actually want to support this situation, then this should work:
       *
       * val event1 = Event(Payload1("abcd"))
       * persisters.unpersist(persisters.persist(event1)) should equal(event1)
       * val event2 = Event(Payload2(42))
       * persisters.unpersist(persisters.persist(event2)) should equal(event2)
       */
    }
  }
}

object OverlappingPersisterSpecDomain {
  case class Event[P](payload: P)
  case class Payload1(msg: String)
  case class Payload2(value: Int)
}
