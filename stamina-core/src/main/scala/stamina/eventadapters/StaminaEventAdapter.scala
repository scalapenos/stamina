package stamina
package eventadapters

import akka.persistence.journal._

/**
 * EventAdapter that uses Stamina to convert events.
 */
class StaminaEventAdapter[P <: AnyRef](persisters: Persisters[P]) extends EventAdapter {
  def this(persisters: List[Persister[_, P, _]]) = this(Persisters(persisters))
  def this(persister: Persister[_, P, _], persisters: Persister[_, P, _]*) = this(Persisters(persister :: persisters.toList))

  def manifest(event: Any) =
    persisters.manifest(event.asInstanceOf[AnyRef]).manifest

  def fromJournal(event: Any, manifest: String) =
    EventSeq(persisters.unpersist(event.asInstanceOf[AnyRef], Manifest(manifest)))

  def toJournal(event: Any) =
    persisters.persist(event.asInstanceOf[AnyRef])
}
