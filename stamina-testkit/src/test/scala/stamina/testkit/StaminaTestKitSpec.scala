package stamina
package testkit

import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

abstract class StaminaTestKitSpec
  extends AnyWordSpecLike
  with Matchers
  with OptionValues
  with TryValues
  with Inside
  with Inspectors

