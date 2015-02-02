package stamina

import json.SprayJsonFormats._
import json.SprayJsonPersistence._
import spray.json.lenses.JsonLenses._

object TestDomain {
  type ItemId = Int
  type CartId = Int

  // ==========================================================================
  // V1
  // ==========================================================================

  case class Item(id: ItemId, name: String)
  case class Cart(id: CartId, items: List[Item])
  case class CartCreated(cart: Cart)

  val item1 = Item(1, "Wonka Bar")
  val item2 = Item(2, "Everlasting Gobstopper")
  val cart = Cart(1, List(item1, item2))
  val cartCreated = CartCreated(cart)

  val v1CartCreatedPersister = persister[CartCreated]("cart-created")

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

  val v2CartCreatedPersister = persister[CartCreatedV2, V2]("cart-created",
    from[V1].to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
  )

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

  val v3CartCreatedPersister = persister[CartCreatedV3, V3]("cart-created",
    from[V1]
      .to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
      .to[V3](_.update('cart / 'timestamp ! set[Long](System.currentTimeMillis)))
  )
}
