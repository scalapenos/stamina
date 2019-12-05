package stamina

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

abstract class StaminaSpec
  extends AnyWordSpecLike
  with Matchers
  with OptionValues
  with TryValues
  with Inside
  with Inspectors
