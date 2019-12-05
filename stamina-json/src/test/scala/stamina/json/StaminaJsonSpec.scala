package stamina.json

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

abstract class StaminaJsonSpec
  extends AnyWordSpecLike
  with Matchers
  with OptionValues
  with TryValues
  with Inside
  with Inspectors
