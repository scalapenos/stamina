package stamina.json

import spray.json.{ DefaultJsonProtocol, RootJsonFormat }
import spray.json.DefaultJsonProtocol._

object JsonTestDomain {
  type ItemId = Int
  type CartId = Int

  // ==========================================================================
  // V1
  // ==========================================================================

  case class ItemV1(id: ItemId, name: String)

  object ItemV1 {
    implicit val formatter: RootJsonFormat[ItemV1] =
      DefaultJsonProtocol.jsonFormat2(ItemV1.apply)
  }

  case class CartV1(id: CartId, items: List[ItemV1])

  object CartV1 {
    implicit val formatter: RootJsonFormat[CartV1] =
      DefaultJsonProtocol.jsonFormat2(CartV1.apply)
  }

  case class CartCreatedV1(cart: CartV1)

  object CartCreatedV1 {
    implicit val formatter: RootJsonFormat[CartCreatedV1] =
      DefaultJsonProtocol.jsonFormat1(CartCreatedV1.apply)
  }

  val v1Item1 = ItemV1(1, "Wonka Bar")
  val v1Item2 = ItemV1(2, "Everlasting Gobstopper")
  val v1Cart = CartV1(1, List(v1Item1, v1Item2))
  val v1CartCreated = CartCreatedV1(v1Cart)

  // ==========================================================================
  // V2
  // ==========================================================================

  case class ItemV2(id: ItemId, name: String, price: Int)

  object ItemV2 {
    implicit val formatter: RootJsonFormat[ItemV2] =
      DefaultJsonProtocol.jsonFormat3(ItemV2.apply)
  }

  case class CartV2(id: CartId, items: List[ItemV2])

  object CartV2 {
    implicit val formatter: RootJsonFormat[CartV2] =
      DefaultJsonProtocol.jsonFormat2(CartV2.apply)
  }

  case class CartCreatedV2(cart: CartV2)

  object CartCreatedV2 {
    implicit val formatter: RootJsonFormat[CartCreatedV2] =
      DefaultJsonProtocol.jsonFormat1(CartCreatedV2.apply)
  }

  val v2Item1 = ItemV2(1, "Wonka Bar", 500)
  val v2Item2 = ItemV2(2, "Everlasting Gobstopper", 489)
  val v2Cart = CartV2(1, List(v2Item1, v2Item2))
  val v2CartCreated = CartCreatedV2(v2Cart)

  // ==========================================================================
  // V3
  // ==========================================================================

  case class ItemV3(id: ItemId, name: String, price: Int)

  object ItemV3 {
    implicit val formatter: RootJsonFormat[ItemV3] =
      DefaultJsonProtocol.jsonFormat3(ItemV3.apply)
  }

  case class CartV3(id: CartId, items: List[ItemV3])

  object CartV3 {
    implicit val formatter: RootJsonFormat[CartV3] =
      DefaultJsonProtocol.jsonFormat2(CartV3.apply)
  }

  case class CartCreatedV3(cart: CartV3, timestamp: Long)

  object CartCreatedV3 {
    implicit val formatter: RootJsonFormat[CartCreatedV3] =
      DefaultJsonProtocol.jsonFormat2(CartCreatedV3.apply)
  }

  val v3Item1 = ItemV3(1, "Wonka Bar", 500)
  val v3Item2 = ItemV3(2, "Everlasting Gobstopper", 489)
  val v3Cart = CartV3(1, List(v3Item1, v3Item2))
  val v3CartCreated = CartCreatedV3(v3Cart, System.currentTimeMillis)
}
