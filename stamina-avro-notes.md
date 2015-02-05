
# Notes on building stamina-avro

1. use a macro to generate an Avro schema from a Scala case class
2. use the schema to serialize and deserialize instances of that class
3. create migrations by post-procesing the schema to add support for older data

Possible migrations from V1 to V2:

- a field was renamed in V2: add a V1 field name alias to the V2 schema
- a field was added in V2: provide a default value in the V2 schema
- a field was removed in V2: it will already be ignored by the V2 schema
- a nested class was renamed/moved in V2: add an alias to its record/enum in the V2 schema

