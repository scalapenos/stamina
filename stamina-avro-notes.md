
# Notes on building stamina-avro

1. use a macro to generate an Avro schema from a Scala case class
2. use the schema to serialize and deserialize instances of that class
3. create migrations by post-procesing the schema to add support for older data

This way we probably won't need to store the actual schema since the reading party will always be the same party that wrote it and the current domain plus any associated migrations should be enough information to generate the schema.

If at all possible, use macros, type classes, etc. to bridge to an existing JVM implementation of Avro, like the Java driver.

### Possible migrations from V1 to V2

- a field was renamed in V2: add a V1 field name alias to the V2 schema
- a field was added in V2: provide a default value in the V2 schema
- a nested class was renamed/moved in V2: add an alias to its record/enum in the V2 schema

### Migrations that are natively supported by avro

- the order of fields has changed in V2
- a field was removed in V2

## Links

- julianpeeters/avro-scala-macro-annotations
- scalavro

