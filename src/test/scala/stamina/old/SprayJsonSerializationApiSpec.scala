package stamina.old

import org.scalatest._

import spray.json._
import DefaultJsonProtocol._

import SprayJsonSerializationSupport._

class SprayJsonSerializationApiSpec extends WordSpecLike with Matchers with OptionValues with TryValues with Inside with Inspectors {
  import SprayJsonSerializationApiSpec._

  "Using fully specified JsonMigration instances, the SprayJson serialization API" should {
    val serializer = akkaSerializerUsing(TestDomainSerialization)

    import serializer._

    "correctly serialize and deserialize the current version of the domain" in {
      fromBinary(toBinary(v4Foo)) should equal(v4Foo)
    }

    "correctly migrate and deserialize older serialized data to the current domain" in {
      fromBinary(toBinary(v1Foo)) should equal(v4Foo)
      fromBinary(toBinary(v2Foo)) should equal(v4Foo)
      fromBinary(toBinary(v3Foo)) should equal(v4Foo)
    }
  }

  "Using JsonMigrationStep instances, the SprayJson serialization API" should {
    val serializer = akkaSerializerUsing(TestDomainSerializationUsingSteps)

    import serializer._

    "correctly serialize and deserialize the current version of the domain" in {
      fromBinary(toBinary(v4Foo)) should equal(v4Foo)
    }

    "correctly migrate and deserialize older serialized data to the current domain" in {
      fromBinary(toBinary(v1Foo)) should equal(v4Foo)
      fromBinary(toBinary(v2Foo)) should equal(v4Foo)
      fromBinary(toBinary(v3Foo)) should equal(v4Foo)
    }
  }

  "When defining migrations, the sprayJsonSerializationWithMigrations function" should {
    "validate that the list of migrations is complete (i.e. it contains migrations for each version from 1 up to the current version" in {
      an[IllegalArgumentException] should be thrownBy {
        sprayJsonSerializationWithMigrations[FooV4]("foo", 4, jsonFormat3(FooV4))(
          JsonMigration(1, renameItems && setSizeToDefault && convertShopperIdToEventContext),
          JsonMigration(3, convertShopperIdToEventContext)
        )
      }

      an[IllegalArgumentException] should be thrownBy {
        sprayJsonSerializationWithMigrations[FooV4]("foo", 4, jsonFormat3(FooV4))(
          JsonMigration(2, renameItems && setSizeToDefault && convertShopperIdToEventContext),
          JsonMigration(3, convertShopperIdToEventContext)
        )
      }
    }
  }

  "When defining migration steps, the sprayJsonSerializationWithMigrationSteps function" should {
    "validate that there is a step to migrate to the current version" in {
      an[IllegalArgumentException] should be thrownBy {
        sprayJsonSerializationWithMigrationSteps[FooV4]("foo", 4, jsonFormat3(FooV4))(
          JsonMigrationStep(1, 2, renameItems),
          JsonMigrationStep(2, 3, setSizeToDefault)
        )
      }
    }

    "validate that there are no missing steps" in {
      an[IllegalArgumentException] should be thrownBy {
        sprayJsonSerializationWithMigrationSteps[FooV4]("foo", 4, jsonFormat3(FooV4))(
          JsonMigrationStep(1, 2, renameItems),
          JsonMigrationStep(3, 4, convertShopperIdToEventContext)
        )
      }

      an[IllegalArgumentException] should be thrownBy {
        sprayJsonSerializationWithMigrationSteps[FooV4]("foo", 4, jsonFormat3(FooV4))(
          JsonMigrationStep(2, 3, setSizeToDefault),
          JsonMigrationStep(3, 4, convertShopperIdToEventContext)
        )
      }
    }

    "validate that there is no step to migrate from the current version" in {
      an[IllegalArgumentException] should be thrownBy {
        sprayJsonSerializationWithMigrationSteps[FooV4]("foo", 4, jsonFormat3(FooV4))(
          JsonMigrationStep(1, 4, renameItems),
          JsonMigrationStep(4, 5, setSizeToDefault)
        )
      }
    }

    "validate that there is no step to migrate to the oldest version" in {
      an[IllegalArgumentException] should be thrownBy {
        sprayJsonSerializationWithMigrationSteps[FooV4]("foo", 4, jsonFormat3(FooV4))(
          JsonMigrationStep(2, 4, renameItems),
          JsonMigrationStep(1, 1, setSizeToDefault)
        )
      }
    }

    "validate that there is are no overlapping steps" in {
      an[IllegalArgumentException] should be thrownBy {
        sprayJsonSerializationWithMigrationSteps[FooV4]("foo", 4, jsonFormat3(FooV4))(
          JsonMigrationStep(1, 4, renameItems),
          JsonMigrationStep(2, 4, setSizeToDefault)
        )
      }

      an[IllegalArgumentException] should be thrownBy {
        sprayJsonSerializationWithMigrationSteps[FooV4]("foo", 4, jsonFormat3(FooV4))(
          JsonMigrationStep(1, 4, renameItems),
          JsonMigrationStep(1, 4, setSizeToDefault)
        )
      }

      an[IllegalArgumentException] should be thrownBy {
        sprayJsonSerializationWithMigrationSteps[FooV4]("foo", 4, jsonFormat3(FooV4))(
          JsonMigrationStep(1, 4, renameItems),
          JsonMigrationStep(1, 3, setSizeToDefault)
        )
      }
    }
  }
}

object SprayJsonSerializationApiSpec {
  type Id = String
  case class EventContext(shopper: Id, session: Id, request: Id)

  case class FooV1(shopperId: Id, items: List[String])
  case class FooV2(shopperId: Id, renamedItems: List[String])
  case class FooV3(shopperId: Id, renamedItems: List[String], size: Int)
  case class FooV4(context: EventContext, renamedItems: List[String], size: Int)

  val shopperId = "123"
  val sessionId = "456"
  val requestId = "789"

  val items = List("item1", "item2", "item3")
  val defaultSize = 42

  val v1Foo = FooV1(shopperId, items)
  val v2Foo = FooV2(shopperId, items)
  val v3Foo = FooV3(shopperId, items, defaultSize)
  val v4Foo = FooV4(EventContext(shopperId, sessionId, requestId), items, defaultSize)

  import spray.json.lenses.JsonLenses._

  implicit val jsonEventContext = jsonFormat3(EventContext.apply)

  val renameItems = JsonTransformer(json ⇒ json.update('renamedItems ! set(json.extract[List[String]]('items))))
  val setSizeToDefault = JsonTransformer(json ⇒ json.update('size ! set(defaultSize)))
  val convertShopperIdToEventContext = JsonTransformer(json ⇒ json.update('context ! set(EventContext(json.extract[Id]('shopperId), sessionId, requestId))))

  val TestDomainSerialization = sprayJsonSerializationWithMigrations[FooV4]("foo", 4, jsonFormat3(FooV4))(
    JsonMigration(1, renameItems && setSizeToDefault && convertShopperIdToEventContext),
    JsonMigration(2, setSizeToDefault && convertShopperIdToEventContext),
    JsonMigration(3, convertShopperIdToEventContext)
  )

  val TestDomainSerializationUsingSteps = sprayJsonSerializationWithMigrationSteps[FooV4]("foo", 4, jsonFormat3(FooV4))(
    JsonMigrationStep(1, 2, renameItems),
    JsonMigrationStep(2, 3, setSizeToDefault),
    JsonMigrationStep(3, 4, convertShopperIdToEventContext)
  )

  // Only required to be able to write the old vrsions in order to test our deserializers
  val TestDomainOldVersionsSerialization = sprayJsonSerialization[FooV1]("foo", 1, jsonFormat2(FooV1.apply)) orElse // format: OFF
                                           sprayJsonSerialization[FooV2]("foo", 2, jsonFormat2(FooV2.apply)) orElse
                                           sprayJsonSerialization[FooV3]("foo", 3, jsonFormat3(FooV3.apply)) // format: ON

  def akkaSerializerUsing(serialization: Serialization): AkkaPersistenceMetaSerializer = {
    new AkkaPersistenceMetaSerializer {
      val serializers = serialization.serializers orElse TestDomainOldVersionsSerialization.serializers
      val deserializers = serialization.deserializers
    }
  }
}
