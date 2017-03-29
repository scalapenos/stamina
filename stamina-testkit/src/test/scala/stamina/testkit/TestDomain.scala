package stamina.testkit

object TestDomain {
  type ItemId = Int

  case class Item(id: ItemId, name: String)

  object Level1 { object Level2 { case object Level3 } }

  val item1 = Item(1, "Wonka Bar")
  val item2 = Item(2, "Everlasting Gobstopper")
}
