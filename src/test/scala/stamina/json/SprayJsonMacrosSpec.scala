package stamina
package json

class SprayJsonMacrosSpec extends StaminaSpec {
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
