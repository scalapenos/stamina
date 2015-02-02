package stamina
package json

import org.scalatest._

class SprayJsonMacrosSpec extends WordSpecLike with Matchers with OptionValues with TryValues with Inside with Inspectors {
  import TestDomain._
  import spray.json._
  import SprayJsonFormats._

  "SprayJsonMacros" should {
    "generate RootJsonFormats for case classes" in {
      // the test is simply that this line should compile
      implicitly[RootJsonFormat[CartCreated]]
    }

    "generate lazy RootJsonFormats for recursive case classes" in {

    }
  }
}
