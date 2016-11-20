package stamina.testkit

object TestDomain {
  type ItemId = Int

  case class Item(id: ItemId, name: String)

  val item1 = Item(1, "Wonka Bar")
  val item2 = Item(2, "Everlasting Gobstopper")
}
