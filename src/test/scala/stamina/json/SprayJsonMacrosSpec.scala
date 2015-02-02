package stamina
package json

import org.scalatest._

class SprayJsonMacrosSpec extends WordSpecLike with Matchers with OptionValues with TryValues with Inside with Inspectors {
  import TestDomain._
  import SprayJsonFormats._


  "SprayJsonMacros" should {
    "generate RootJsonFormats for case classes" in {
      // the test is simply that these lines should compile
      implicitly[RootJsonFormat[ItemAdded]]
      implicitly[RootJsonFormat[ItemRemoved]]
      implicitly[RootJsonFormat[CartCreated]]
      implicitly[RootJsonFormat[CartUpdated]]
      implicitly[RootJsonFormat[CartDestroyed]]
    }

    "generate lazy RootJsonFormats for recursive case classes" in {

    }
  }
}
