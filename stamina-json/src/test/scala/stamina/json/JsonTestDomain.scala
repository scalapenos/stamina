package stamina

object JsonTestDomain {
  type ItemId = Int
  type CartId = Int

  // ==========================================================================
  // V1
  // ==========================================================================

  case class ItemV1(id: ItemId, name: String)
  case class CartV1(id: CartId, items: List[ItemV1])
  case class CartCreatedV1(cart: CartV1)

  val v1Item1 = ItemV1(1, "Wonka Bar")
  val v1Item2 = ItemV1(2, "Everlasting Gobstopper")
  val v1Cart = CartV1(1, List(v1Item1, v1Item2))
  val v1CartCreated = CartCreatedV1(v1Cart)

  // ==========================================================================
  // V2
  // ==========================================================================

  case class ItemV2(id: ItemId, name: String, price: Int)
  case class CartV2(id: CartId, items: List[ItemV2])
  case class CartCreatedV2(cart: CartV2)

  val v2Item1 = ItemV2(1, "Wonka Bar", 500)
  val v2Item2 = ItemV2(2, "Everlasting Gobstopper", 489)
  val v2Cart = CartV2(1, List(v2Item1, v2Item2))
  val v2CartCreated = CartCreatedV2(v2Cart)

  // ==========================================================================
  // V3
  // ==========================================================================

  case class ItemV3(id: ItemId, name: String, price: Int)
  case class CartV3(id: CartId, items: List[ItemV3])
  case class CartCreatedV3(cart: CartV3, timestamp: Long)

  val v3Item1 = ItemV3(1, "Wonka Bar", 500)
  val v3Item2 = ItemV3(2, "Everlasting Gobstopper", 489)
  val v3Cart = CartV3(1, List(v3Item1, v3Item2))
  val v3CartCreated = CartCreatedV3(v3Cart, System.currentTimeMillis)
}
