
# Misc TODOs

extract Migration, Migrator, and the migration DSL as abstract stuff for reuse in the avro implementation

Demonstrate that you can combine two different Persister implementations
in one instance of Persisters

## Migrating between different implementations?
Allow for switching implementations halfway? Something like this:

    json.persister[T, V3](...) migrateTo avro.persister[T, V4](...)

or perhaps specified the other way around?

    avro.persister[T, V4](...) migrateFrom json.persister[T, V3](...)


# Notes on Stamina Testkit

- per `Persister[T, V]` + sample instance of T
    + generate a roundtrip testcase to prove basic behavior
    + if no regression test file exists, generate a file containing the persisted bytes, named using the relevant key and the version
    + if a regression test file exists for the current version, unpersist it to make sure no incompatible changes were made without incrementing the version
    + for all versions before the current version, assume a regression test file exists and use it to unpersist the persisted data to verify migrations work correctly
- allow testing at the `Persister[T, V]` level or at the `Persisters` level
- you can always get the key and the version required to check for regression test files by simply persisting the same instance of T and getting them from the instance of `Persisted` produced.


# Notes on the Stamina example app

- build a basic Spray/Akka CRUD app using persistent actors per resource + id
- use a simplified version of the cluster-sharding approach, with one parent actor serving as the API but delegating to persistent child actors
- the persistent child actors kill themselves after x minutes of inactivity
- use file-based level db as the akka persistence store
- start out with all domain classes at V1 and then migrate the data in all supported ways, making sure to go up to V5+
- provide an app (or a version of it) for every supported implementation (i.e. json, avro, etc.)
- add some timing/sizing benchmarking code so people can compare performance
- perhaps also add a version using the default Akka implementation so we can compare performance to that


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


