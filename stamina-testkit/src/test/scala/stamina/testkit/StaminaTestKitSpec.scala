package stamina
package testkit

import org.scalatest._

abstract class StaminaTestKitSpec
  extends WordSpecLike
  with Matchers
  with OptionValues
  with TryValues
  with Inside
  with Inspectors

