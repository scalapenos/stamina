package stamina

import scala.reflect.runtime.universe._

/**
 * This marker interface can be used to solve the problem of nested json formats of the same
 * root format.
 * By example:
 * trait Event[E] {
 * }
 *
 * case class Payload1()
 * case class Payload2()
 *
 * The Persister cannot distinguish Event[Payload1] from Event[Payload2] due to type erasure within
 * Akka serialization to AnyRef. Therefore you can mark your Event envelop using a TypeTagged marker
 * interface which whould allow stamina to choose the correct persister for the kind of event payload
 * which should get serialized.
 */
class TypeTagged[X: TypeTag] extends AnyRef {
  @transient val tag = typeTag[X]
}