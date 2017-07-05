package stamina.migrations

import stamina._

class MigratorSpec extends StaminaSpec {

  val migrator = from[String, V1]
    .to[V2](_ + "V2")
    .backTo[V1](_.replace("V2", ""))
    .to[V3](_ + "V3")
    .backTo[V2](_.replace("V3", ""))
    .to[V4](_ + "V4")
    .backTo[V3](_.replace("V4", ""))
    .to[V5](_ + "V5")

  "Migrator V5" should {
    "be able to migrate" when {
      "migration is from V1 to V5" in {
        migrator.canMigrate(1, 5) shouldBe true
      }

      "migration is from V1 to V1" in {
        migrator.canMigrate(1, 1) shouldBe true
      }

      "migration is from V5 to V2" in {
        migrator.canMigrate(5, 2) shouldBe true
      }
    }

    "not be able to migrate" when {
      "migration is from V6 to V2" in {
        migrator.canMigrate(6, 2) shouldBe false
      }

      "migration is from V6 to V7" in {
        migrator.canMigrate(6, 7) shouldBe false
      }

      "migration is from V1 to V7" in {
        migrator.canMigrate(1, 7) shouldBe false
      }
    }

    "migrate forward" when {
      "migration is from V1 to V5" in {
        migrator.migrate("V1", 1, 5) shouldBe "V1V2V3V4V5"
      }

      "migration is from V2 to V5" in {
        migrator.migrate("V1V2", 2, 5) shouldBe "V1V2V3V4V5"
      }

      "migration is from V1 to V4" in {
        migrator.migrate("V1", 1, 4) shouldBe "V1V2V3V4"
      }

      "migration is from V1 to V1" in {
        migrator.migrate("V1", 1, 1) shouldBe "V1"
      }

      "migration is from V2 to V2" in {
        migrator.migrate("V1V2", 2, 2) shouldBe "V1V2"
      }
    }

    "migrate backward" when {
      "migration is from V2 to V1" in {
        migrator.migrate("V1V2", 2, 1) shouldBe "V1"
      }

      "migration is from V5 to V1" in {
        migrator.migrate("V1V2V3V4V5", 5, 1) shouldBe "V1V5"
      }

      "migration is from V5 to V2" in {
        migrator.migrate("V1V2V3V4V5", 5, 2) shouldBe "V1V2V5"
      }

      "migration is from V5 to V3" in {
        migrator.migrate("V1V2V3V4V5", 5, 3) shouldBe "V1V2V3V5"
      }
    }
  }
}
