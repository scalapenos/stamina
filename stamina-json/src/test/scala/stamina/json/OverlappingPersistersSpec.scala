package stamina
package json

import spray.json._
import DefaultJsonProtocol._

class OverlappingPersisterSpec extends StaminaJsonSpec {
  "The persisters construction DSL" should {
    case class Event[P](payload: P)
    case class Payload1(msg: String)
    case class Payload2(value: Int)

    implicit val payload1Format = jsonFormat1(Payload1)
    implicit val payload2Format = jsonFormat1(Payload2)

    implicit val eventPayload1Format = jsonFormat1(Event[Payload1])
    implicit val eventPayload2Format = jsonFormat1(Event[Payload2])

    // TODO #43 make sure we fail at compile (or perhaps initialization) time, not at persist
    "correctly handle overlapping persisters" ignore {
      val persisters = Persisters(
        persister[Event[Payload1]]("payload1"),
        persister[Event[Payload2]]("payload2")
      )

      val event1 = Event(Payload1("abcd"))
      persisters.unpersist(persisters.persist(event1)) should equal(event1)

      val event2 = Event(Payload2(42))
      persisters.unpersist(persisters.persist(event2)) should equal(event2)
    }
  }
}
