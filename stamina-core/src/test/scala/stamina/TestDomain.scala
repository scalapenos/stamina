package stamina

object TestDomain {
  type ItemId = Int
  type CartId = Int

  case class Item(id: ItemId, name: String)
  case class Cart(id: CartId, items: List[Item])
  case class CartCreated(cart: Cart)

  val item1 = Item(1, "Wonka Bar")
  val item2 = Item(2, "Everlasting Gobstopper")
  val cart = Cart(1, List(item1, item2))
  val cartCreated = CartCreated(cart)
}
