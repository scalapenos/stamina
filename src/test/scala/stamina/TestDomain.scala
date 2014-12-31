package stamina

trait TestDomain {
  type ItemId = Int
  type CartId = Int

  case class Item(id: ItemId, name: String)
  case class Cart(id: CartId, items: List[Item])

  case class ItemAdded(item: Item)
  case class ItemRemoved(item: Item)

  case class CartCreated(cart: Cart)
  case class CartUpdated(cart: Cart)
  case class CartDestroyed(cart: Cart)

  val item1 = Item(1, "Wonka Bar")
  val item2 = Item(2, "Everlasting Gobstopper")

  val cart = Cart(1, List(item1, item2))

  val itemAdded = ItemAdded(item1)
  val itemRemoved = ItemRemoved(item2)

  val cartCreated = CartCreated(cart)
  val cartUpdated = CartUpdated(cart)
  val cartDestroyed = CartDestroyed(cart)

  // TODO: remove these as soon as we have our macro implementation
  import spray.json._
  import DefaultJsonProtocol._
  implicit val itemFormat = jsonFormat2(Item)
  implicit val cartFormat = jsonFormat2(Cart)
  implicit val itemAddedFormat = jsonFormat1(ItemAdded)
  implicit val itemRemovedFormat = jsonFormat1(ItemRemoved)
  implicit val cartCreatedFormat = jsonFormat1(CartCreated)
  implicit val cartUpdatedFormat = jsonFormat1(CartUpdated)
  implicit val cartDestroyedFormat = jsonFormat1(CartDestroyed)
}

object TestDomain extends TestDomain
