package stamina
package json

class ByteArrayPersisterSpec extends StaminaJsonSpec {
  import JsonTestDomain._
  import spray.json.lenses.JsonLenses._
  import fommil.sjs.FamilyFormats._

  val jsonPersister = persister[CartCreatedV2, V2]("cart-created",
    from[V1].to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
  )

  val persisters = toByteArrayPersisters(List(jsonPersister))

  "The conversion to Persisters[Array[Byte]]" should {
    "produce a persister that implements a roundtrip" in {
      import persisters._
      val serialized: Persisted[Array[Byte]] = persist(v2CartCreated)
      serialized.manifest should equal(jsonPersister.currentManifest)
      unpersist(serialized) should equal(v2CartCreated)
    }
  }
}
