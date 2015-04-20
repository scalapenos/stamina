package stamina

import scala.reflect.ClassTag

import org.apache.avro._
import org.apache.avro.generic._

import migrations._

/**
 * An implementation of the stamina Persister that will use Apache Avro
 * to read/write serialized values. It supports a DSL for specifying
 * migrations to apply to older versions of persisted values to bring
 * them up to date with the latest version.
 *
 *
 * If you rename a field in V2, the Avro schema used for reading V1 will
 * be the same as the schema for V2 but with an extra alias for the old field name
 *
 * If you add a field in V2, the Avro schema used for reading V1 will
 * be the same as the schema for V2 but it will set a default value for that field,
 * which defaults to "null" but it can also be specified explicitly.
 *
 * If you remove a field in V2, the Avro schema used for reading V1 will
 * be the same as the schema for V2, which will simply ignore that field.
 *
 *
 * @example
 * {{{
 * import stamina.avro._
 *
 * val p = persister[CartCreated, V3]("cart-created",
 *   from[V1]
 *     .to[V2](addField('cart / 'items / * / 'price, 1000))
 *     .to[V3](renameField('date, 'timestamp) && addOptionalField())
 * )
 * }}}
 *
 */
package object avro {

  // type AvroRecord = GenericRecord

  /** Simple type alias for Migration[AvroSchema] */
  // type AvroMigration = Migration[AvroSchema]

  // /** Simple type alias for Migrator[JsValue, V] */
  // type AvroMigrator[V <: Version] = Migrator[AvroSchema, V]

  /**
   * Creates an AvroMigrator[V1] that can function as a builder for
   * creating AvroMigrator[V2], etc. Its migration will be the identity
   * function so calling its migrate function will not have any effect.
   */
  // def from[V <: V1: VersionInfo]: AvroMigrator[V] = migrations.from[JsValue, V]

  /**
   * Creates an AvroPersister[T, V1], i.e. an AvroPersister that will only persist
   * and unpersist version 1. Use this function to produce the initial persister
   * for a new domain class/event/entity.
   */
  def persister[T: ClassTag](key: String): AvroPersister[T, V1] = new V1AvroPersister[T](key) //, AvroMacros.schemaFor[T])

  /**
   * Creates an AvroPersister[T, V] where V is a version greater than V1.
   * It will always persist instances of T to version V but it will use the specified
   * AvroMigrator[V] to migrate any values older than version V to version V before
   * unpersisting them.
   */
  // def persister[T: AvroFormat: ClassTag, V <: Version: VersionInfo: MigratableVersion](key: String, migrator: AvroMigrator[V]): AvroPersister[T, V] = new VnAvroPersister[T, V](key, migrator)

  private[avro] def toAvroBinaryBytes[T](t: T): ByteString = {

    // we need an avro schema
    // we need a way to turn the instance of T into a generic record using the schema -> AvroFormat
    // we need to turn the genericrecord into bytes

    // val schema = AvroMacros.schemaFor[T]

    // println("---------------> ")

    ByteString.empty
  }

  private[avro] def fromAvroBinaryBytes[T](bytes: ByteString): T = {

    // we need the schema
    // we use the schema to read a generic record
    // we need a way to turn the genericRecord into an instance of T (schema not needed?)

    return null.asInstanceOf[T]
  }
}

package avro {

  /**
   * A class that represents an Avro schema in such a way that:
   *
   * - it can be modified by Migrators (immutably)
   * - it can expose/generate the raw native Schema *by using the native SchemaBuilder
   */
  trait AvroSchema {
    // TODO: methods to modify this schema

    private[avro] def toNative: Schema
  }

  /**
   * Simple type-class for direct, one-to-one translation of
   */
  // trait AvroFormat[T] {
  //   def write(t: T): AvroRecord
  //   def read(bytes: AvroRecord): T
  // }

  /**
   * Simple abstract marker superclass to unify (and hide) the two internal Persister implementations.
   */
  sealed abstract class AvroPersister[T: ClassTag, V <: Version: VersionInfo](key: String) extends Persister[T, V](key) {
    private[avro] def cannotUnpersistMsg(p: Persisted) =
      s"""AvroPersister[${implicitly[ClassTag[T]].runtimeClass.getSimpleName}, V${currentVersion}](key = "${key}") cannot unpersist data with key "${p.key}" and version ${p.version}."""
  }

  private[avro] class V1AvroPersister[T: ClassTag](key: String) extends AvroPersister[T, V1](key) {

    def persist(t: T): Persisted = Persisted(key, currentVersion, toAvroBinaryBytes(t))
    def unpersist(p: Persisted): T = {
      if (canUnpersist(p)) fromAvroBinaryBytes[T](p.bytes)
      else throw new IllegalArgumentException(cannotUnpersistMsg(p))
    }
  }

}
