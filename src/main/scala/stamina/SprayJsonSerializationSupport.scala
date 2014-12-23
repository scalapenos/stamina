package stamina

import scala.reflect._

import spray.json._

/**
 * Adds some handy sugar around reading/writing json from/to ByteStrings.
 */
object SprayJsonSerializationSupport {
  /** Constant indicating the minimum supported version in serialization and migrations. */
  val MinimumVersion = 1

  implicit class AnyWithJsonByteStringConversion[T](any: T) {
    def toJsonBytes(implicit writer: RootJsonWriter[T]): ByteString = ByteString(writer.write(any).compactPrint)
  }

  implicit def byteStringParserInput(bytes: ByteString): ParserInput = new ParserInput.ByteArrayBasedParserInput(bytes.toArray)

  def fromJsonBytes[T](bytes: ByteString)(implicit reader: RootJsonReader[T]): T = {
    reader.read(JsonParser(bytes))
  }

  /** A simple transformation function that takes a JSON AST and transforms it to another JSON AST. */
  case class JsonTransformer(f: JsValue ⇒ JsValue) {
    def apply(in: JsValue) = f(in)
    def &&(next: JsonTransformer) = JsonTransformer(in ⇒ next(f(in)))
  }

  object JsonTransformer {
    implicit def fromFunction(f: JsValue ⇒ JsValue) = JsonTransformer(f)
  }

  /** Utility wrapper around RootJsonReader that applies the specified transformer before reading the result with the specified reader. */
  private[this] case class JsonReaderWithPreProcessor[T](reader: RootJsonReader[T], transformer: JsonTransformer) extends RootJsonReader[T] {
    def read(json: JsValue): T = reader.read(transformer(json))
  }

  /** Defines the Json transformation required to migrate from a specific version to the curent version. */
  case class JsonMigration(fromVersion: Int, transformer: JsonTransformer)

  /** Defines the Json transformation required to migrate from one version to another version. */
  case class JsonMigrationStep(fromVersion: Int, toVersion: Int, transformer: JsonTransformer) {
    require(fromVersion < toVersion, s"JsonMigrations can only happen from a certain version to a higher version. ${toVersion} is not higher than ${fromVersion}")
  }

  def sprayJsonSerialization[T <: AnyRef: ClassTag](keyPrefix: String, currentVersion: Int, currentFormat: RootJsonFormat[T]): Serialization = {
    sprayJsonSerializationWithMigrations(keyPrefix, currentVersion, currentFormat, Nil)
  }

  def sprayJsonSerializationWithMigrations[T <: AnyRef: ClassTag](keyPrefix: String, currentVersion: Int, currentFormat: RootJsonFormat[T])(migrations: JsonMigration*): Serialization = {
    sprayJsonSerializationWithMigrations(keyPrefix, currentVersion, currentFormat, migrations.toList)
  }

  def sprayJsonSerializationWithMigrations[T <: AnyRef: ClassTag](keyPrefix: String, currentVersion: Int, currentFormat: RootJsonFormat[T], migrations: List[JsonMigration] = Nil): Serialization = {
    def keyFor(prefix: String, version: Int) = s"${prefix}-v${version}"

    def deserializerFor(migration: JsonMigration): Deserializers = {
      case Serialized(key, bytes) if key == keyFor(keyPrefix, migration.fromVersion) ⇒ fromJsonBytes[T](bytes)(JsonReaderWithPreProcessor(currentFormat, migration.transformer))
    }

    // STEP 1: make sure the list of migrations is complete
    if (!migrations.isEmpty) {
      val fromVersions = migrations.map(_.fromVersion)
      val expectedFromVersions = Range(MinimumVersion, currentVersion - 1)

      if (!expectedFromVersions.forall(fromVersions.contains(_))) throw new IllegalArgumentException("Incomplete list of migrations. One or more steps missing.")
    }

    // STEP 2: Create a serializer and deserializer for the current version
    val currentKey = keyFor(keyPrefix, currentVersion)
    val currentVersionSerializers: Serializers = {
      case event: T ⇒ Serialized(currentKey, event.toJsonBytes(currentFormat))
    }

    val currentVersionDeserializers: Deserializers = {
      case Serialized(key, bytes) if key == currentKey ⇒ fromJsonBytes[T](bytes)(currentFormat)
    }

    // STEP 3: Create and compose Deserializers for the migrations
    val deserializers = migrations.map(deserializerFor(_)).foldLeft(currentVersionDeserializers)((acc, des) ⇒ acc orElse des)

    Serialization(currentVersionSerializers, deserializers)
  }

  def sprayJsonSerializationWithMigrationSteps[T <: AnyRef: ClassTag](keyPrefix: String, currentVersion: Int, currentFormat: RootJsonFormat[T])(migrationSteps: JsonMigrationStep*): Serialization = {
    sprayJsonSerializationWithMigrationSteps(keyPrefix, currentVersion, currentFormat, migrationSteps.toList)
  }

  def sprayJsonSerializationWithMigrationSteps[T <: AnyRef: ClassTag](keyPrefix: String, currentVersion: Int, currentFormat: RootJsonFormat[T], migrationSteps: List[JsonMigrationStep] = Nil): Serialization = {
    def stepsToMigrations(steps: List[JsonMigrationStep]): List[JsonMigration] = {
      @scala.annotation.tailrec
      def go(in: List[JsonMigrationStep], out: List[JsonMigrationStep] = Nil): List[JsonMigrationStep] = {
        in match {
          case Nil          ⇒ out
          case head :: tail ⇒ go(tail, tail.foldLeft(head)((acc, step) ⇒ JsonMigrationStep(acc.fromVersion, step.toVersion, acc.transformer && step.transformer)) :: out)
        }
      }

      go(steps).map(combined ⇒ JsonMigration(combined.fromVersion, combined.transformer))
    }

    // STEP 1: make sure the migration steps are sorted because they will be applied in order
    val sortedSteps = migrationSteps.sortBy(_.fromVersion)

    // STEP 2: validate the list of migration steps
    val maxVersion = sortedSteps.map(_.toVersion).max

    if (maxVersion != currentVersion) throw new IllegalArgumentException("Incomplete list of migrations. No path to the current version defined.")

    val (fromVersions, toVersions) = sortedSteps.map(m ⇒ (m.fromVersion, m.toVersion)).unzip

    if (fromVersions.toSet.size != fromVersions.size) throw new IllegalArgumentException("The list of migrations contains conflicting or duplicate entries.")
    if (toVersions.toSet.size != toVersions.size) throw new IllegalArgumentException("The list of migrations contains conflicting or duplicate entries.")
    if (fromVersions.contains(maxVersion)) throw new IllegalArgumentException(s"Invalid migration. The current version (${currentVersion}) is not allowed as the source of a migration.")

    val minVersion = sortedSteps.map(_.fromVersion).min

    if (minVersion != MinimumVersion) throw new IllegalArgumentException(s"Incomplete list of migrations. There is no migration that starts with the minimum version (i.e. v${MinimumVersion}).")
    if (toVersions.contains(minVersion)) throw new IllegalArgumentException(s"Invalid migration. The lowest version (${minVersion}) is not allowed as the target of a migration.")

    val expectedFromVersions = Range(minVersion, currentVersion - 1)
    val expectedToVersions = Range(minVersion + 1, currentVersion)

    if (!expectedFromVersions.forall(fromVersions.contains(_))) throw new IllegalArgumentException("Incomplete list of migrations. One or more steps missing.")
    if (!expectedToVersions.forall(toVersions.contains(_))) throw new IllegalArgumentException("Incomplete list of migrations. One or more steps missing.")

    // STEP 3: transform the list of migration steps to a list of migrations
    sprayJsonSerializationWithMigrations(keyPrefix, currentVersion, currentFormat, stepsToMigrations(sortedSteps))
  }
}
