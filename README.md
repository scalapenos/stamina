
# THIS PROJECT/README IS UNDER CONSTRUCTION!
Stamina is not ready for prime-time yet but that will hopefully change soon. Do feel free to browse, comment, create issues, pull requests, etc. as long as you realize that nothing has been set into stone yet.

## Current status
*stamina-core* and *stamina-json* are looking quite finished already and we're happy wth the API that has evolved out of the many experiments we tried recently. The core API might change slightly when we try to implement a second serialization framework and we might prmote (parts of) of the migration DSL from stamina-json into the core API.

Next modules to be built: *stamina-testkit* and *stamina-sample-app*.

Possible alternative implementations being looked at: *stamina-avro*, *stamina-pickling*.

The contents of this README will be frequently updated to reflect the current status.


# Stamina
Stamina is an Akka Serializer toolkit written specifically for use with Akka
Persistence. Its main defining characteristics are:

- support for explicitly versioning serialized data. Stamina always stores a version number with the serialized data.
- support for explicitly migrating serialized data written for older versions to the latest version
- support for defining (de)serialization behavior in code. This includes (auto-)generating so-called *persisters* from Scala case classes and an API for specifying migration behaviors.
- decoupling from fully qualified class names (or randomly generated ids) as serialization keys. Instead, Stamina uses simple String keys to identify serialized types.
- support for multiple serialization libraries as implementation plugins, as long as they can be ported/adjusted/wrapped in order to support the above features.

The first (and currently only) implementation is based on spray-json. It supports migration from oldder versions using a very simple little DSL to pre-process the JSON AST based on the specific version being read before deserialization takes place. Here's an example:

```scala
// This example uses explicitly versioned case classes to more
// easily show how to deal with explicit versions and migrations.
// Normally, of course, you would only need one case class,
// which would always represent the current version (V3 in this case).
import stamina.json._
import stamina.json.SprayJsonFormats._
import spray.json.lenses.JsonLenses._

// spray-json persister for V1.
// Essentially equivalent to any existing Akka serializer except
// for the simple API used to specify/generate them.
val v1CartCreatedPersister = persister[CartCreatedV1]("cart-created")

// spray-json persister for V2 but with support for migration
// of data writen in the V1 format.
val v2CartCreatedPersister = persister[CartCreatedV2, V2]("cart-created",
from[V1].to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
)

// spray-json persister for V3 but with support for migration
// of data writen in the V1 and V2 formats.
val v3CartCreatedPersister = persister[CartCreatedV3, V3]("cart-created",
from[V1]
  .to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
  .to[V3](_.update('timestamp ! set[Long](System.currentTimeMillis - 3600000L)))
)
```

An example application and a testkit with support for regression
testing of serialized older versions are currently being worked on.

Support for Apache Avro and Scala's own pickling formats is pending.


## Why?
Stamina aims to be the serialization toolkit that should have come
with Akka Persistence but didn't.

### So what's wrong with Akka Persistence?
Akka Persistence is an awesome library for implementing event
stores and event-sourcd persistent actors.

The problem arises from the fact that Akka Persistence reuses the
standard Akka serialization system without adding any kind of
support for data versioning or deserialization of older versions of
persisted data.

This has lead to people having to maintain multiple older versions
of their domain/event model in parallel or to somehow work around
this limitation. This project has grown out of one such workaround.

The Akka serialization system was originally written to support remote
actors and clustered actors. It is optimized for raw performance and
low configuration overhead but it assumes that the code that
serializes your data is the exact same code that deserializes it
later. This is not necessarily the case in an event store, where data
might have been serialized by an older iteration of your code.

An event store without some kind of versioning/migration system is not
very useful in our opinion so we set out to provide these features in
a way that is backwards compatible with Akka Persistence.

Some other problems with the existing Akka serialization system
(and many other serialization libraries) are:

- coupling between the serialized data and a specific Java/Scala class
or class name, usually a fully qualified class one. This creates a
barrier to refactoring of the domain model, something that is very
necessary to keep active code bases healthy and clean.

- no high-level API for configuring which classes get serialized how.
The only available option is linking the fully qualified class name of
a specific class (or superclass) to the fully qualified classname of a
serializer. This allows for very little flexibility, no support for
versioning or migrations, and leads to having to write lots of explicit,
low-level serialization code.

Most of these problems arise from the simple fact that versioning and
migration were never part of the design.


## Goals / Approach

1. Stamina should support pluggable serialization implementations so people can reuse their existing formats and libraries.
2. Stamina should support code-level configuration options like type classes and macros so it can use all the power provided by the existing libraries.
3. Stamina should provide explicit support for versioning serialized data (i.e. data is always written together with a version number).
4. Stamina should provide support for decoupling from the fully qualified class name of serialized classes (i.e. data is always written together with an explicit symbolic key),
5. Stamina should provide explicit and easy to use support for deserializing older versions of stored classes (i.e. migrations).
6. Stamina should be highly customizable from code while requiring the least possible amount of boilerplate
7. Stamina should use the Scala type system to prevent common serialization, versioning, and migration problems.
