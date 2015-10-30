package stamina
package eventadapters

import akka.persistence.journal._

/**
 * EventAdapter that uses Stamina to convert events to and from Array[Byte].
 *
 * When used like this, there is little value in using an EventAdapter instead of Serializer.
 * We plan to generalize this to allow persisters for other things like Array[Byte], though.
 */
class StaminaEventAdapter(persisters: Persisters) extends EventAdapter {
  def this(persisters: List[Persister[_, _]]) = this(Persisters(persisters))
  def this(persister: Persister[_, _], persisters: Persister[_, _]*) = this(Persisters(persister :: persisters.toList))

  def manifest(event: Any) =
    persisters.manifest(event.asInstanceOf[AnyRef]).manifest

  def fromJournal(event: Any, manifest: String) =
    EventSeq(persisters.unpersist(event.asInstanceOf[Array[Byte]], Manifest(manifest)))

  def toJournal(event: Any) =
    persisters.persist(event.asInstanceOf[AnyRef])
}
