package stamina
package json

class SprayJsonMacrosSpec extends StaminaJsonSpec {
  import JsonTestDomain._
  import spray.json._
  import SprayJsonMacros._

  "SprayJsonMacros" should {
    "generate RootJsonFormats for case classes" in {
      // the test is simply that these lines should compile
      implicitly[RootJsonFormat[ItemV1]]
      implicitly[RootJsonFormat[CartV1]]
      implicitly[RootJsonFormat[CartCreatedV1]]
    }

    "generate lazy RootJsonFormats for recursive case classes" ignore {

    }
  }
}
