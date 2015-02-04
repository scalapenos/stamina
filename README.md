Stamina is an Akka Serializer written specifically for use with Akka
Persistence.

It supports explicit versioning of serialized data, an API to support
migration of older data into your current domain model withgout having
to keep those older classes around, an API for hooking in existing
serialization libraries, a ready to use implementation based on spray-
json, a testkit with support for regression/migration testing, and
much more.

## Why?
Stamina aims to be the serialization toolkit that should have come
with Akka Persistence but didn't.

### So what's wrong with Akka Persistence?
Akka Persistence is an awesome library for implementing event stores and event-sourcd persistent actors.

The problem arises from the fact that Akka Persistence reuses the
standard Akka serialization system without adding any kind of
support for data versioning or deserialization of older versions of
persisted data.

The Akka serialization system was originally written to support remote
actors and clustered actors. It is optimized for raw performance and
low configuration overhead but it assumes that the code that
serializes your data is the exact same code that deserializes it
later. This is not necessarily the case in an event store, where data
might have been serialized by an older iteration of your code.

An event store without some kind of versioning/migration system is not
very useful since it forces people to somehow work around the missing
bits, usually by keeping multiple versions of domain classes in the
code base and explicitly having to deal with those older versions in
persistent actors. This leads to lots of duplication and an overly
complex code base.

Some other problems with the existing Akka serialization system (and many serialization libraries) are:

- coupling between the serialized data and a specific Java/Scala class
or class name. This creates a barrier to refactoring of the domain
model, something that is very necessary to keep active code bases
healthy and clean.

- no high-level API for configuring which classes get serialized how.
The only available option is linking the fully qualified class name of
a specific class to the fully qualified classname of a serializer.

Most of these problems arise from the simple fact that versioning and
migration were never part of the design.


### What does Stamina add?
Stamina aims to fix the problems with existing serialization options
by providing an alternative implementation of the Akka Serializer
that:

- supports explicit





## Stamina Goals

### Major

1. It should support pluggable serialization implementations
2. It should support code-level configuration options like type classes and macros
3. It should provide explicit support for versioning serialized data (i.e. data is always written together with a version)
4. It should provide explicit and easy to use support for deserializing older versions of stored classes (i.e. migrations)
5. It should be highly customizable from code while requiring the least possible amount of boilerplate

### Minor

1. Move away from the cumbersome practise of specifying fully qualified classnames in application.conf. Use of FQCNs lead to weird problems, even when using a single marker trait (i.e. not implementing the Java Serializable interface could lead to the wrong serializer being chosen silently). It's also very inflexible and doesn't leave any space for customization.


## Approach

- Store the data together with an explicit key instead of the fully qualified class name to ease refactoring of domain classes/events (i.e a deserializer can be found based purely on the raw serialized data without coupling with the (sometimes outdated) FQCNs).
- Store the data with an explicit version to ease migration from older versions to the current version (i.e. a migration path can be determined based purely on the raw serialized data).
- provide a low-level serialization API for turning an instance of some class (essentially an untyped AnyRef) into the combination of a key, a version, and the raw serialized bytes.
- Provide a low-level deserialization API for turning the combination of a key, a version, and the raw serialized bytes into an instance of some class.
- Provide higher level serialization/deserialization APIs on top of the lower level ones to reduce the amount of code required.
- Use macros to perform compile-time validation of versioning constraints (i.e. to prevent skipping versions, duplicate versions missing migration steps, etc.).


## Notes
Use macros to validate the migration steps.

Make a type class based format system with bridges to spray JSON, etc.

Use macros to generate formats

Generate AST transformers with macros?

Also build a binary implementation based on Kryo. Use macros to generate a Kryo format. But how to implement migrations? Generate intermediate case classes? Use an intermediate AST-like structure like a map? Use compiler trees directly and transform them?


#### Level 0: The Akka Serializer


#### Level 1: The Stamina Low-level Persister
- lowest-level
- converts T => Persisted and Persisted => T
- uses Encoding for the actual (de)serialization

#### Level 2a: Stamina Encodings and Migrators

Encoding:
- low-level
- converts T => ByteString and ByteString => T

Migration:
- used when you only have one domain model and you want to deserialized older versions of that model into the latest version.
- read-only
- converts (Version, ByteString) => T
- can be direct or step-wise
- step-wise needs some inbetween format, usually an AST of some sort
    - Needs ByteString => AST // name? (parser)
    - Needs AST => T          // name?
    - Needs Migrator(s):
        -  AST => AST
        -  List[AST => AST] // sequential steps, macro guarded

#### Level 2b: Specific Implementations of Stamina Encodings and Migrators

JsonMigrator:
- JsonParser => JsValue
- RootJsonReader[T]: JsValue => T
- JsonMigrator
    + JsonMigrator(f: JsValue => JsValue)
    + JsonMigrator(steps: List[JsValue => JsValue])

KryoMigrator:
- Kryo: ByteString => Tuple
- TupleReader: Tuple => T


#### Level 3: The user level

##### Encoding without migration

    persister[Foo]("foo") // default version (1) and implicit serializer
    persister[Foo]("foo", 1) // implicit encoding (through Macros)
    persister[Foo]("foo", 1)(jsonEncoding[Foo]) // explicit encoding
    persister[Foo]("foo", 1)(jsonFormat4[Foo])
        // explicit encoding through implicit conversion from the spray-json
        // typeclass to the Encoder typesclass.

    def persister[T: Encoding](key: String, version: Version = 1)

##### Encoding with migration

    // can the compiler guarantee the (implicit) encoding is the same type as the Migrator?
    persister[Foo]("foo", 3, Migrations(
        jsonMigrator(1, _.update('size ! set(defaultSize))),
        jsonMigrator(2, _.update('state / 'items / * / 'product / 'availableStock ! set[Int](defaultAvailableStock)))
    ))





## Raw Stuff

instead of migration and migrationWithSteps, use an abstract concept of a
Migration[A, B] that has two implementations:

    DirectMigration[A, B](migrate: A => B)
    MigrationInSteps[A, B](a: A, steps: List[A => A], migrate: A => B)

Json migrations are just implementations of these that use implicit macros
to generate the formats (in the companion object of the Migration class)

    JsonMigration[A: RootJsonFormat] extends DirectMigration[A, JsValue]
    JsonMigrationInSteps[A: RootJsonFormat]
      extends MigrationInSteps[A, JsValue]


### Thought Models

    type Version = Int
    Migration: (A, Version) => (A, Version)
    // macros to verify version 1 > version 2

### Later:
Still look into Tuples as potential ASTs for serialization libraries
that don't support ASTs so you can still use steps to post-process them.
But how to do the initial migration from A to Tuple?

