
[![Build Status](https://img.shields.io/travis/scalapenos/stamina.svg)](https://travis-ci.org/scalapenos/stamina)
[![Latest version](https://index.scala-lang.org/scalapenos/stamina/stamina/latest.svg)](https://index.scala-lang.org/scalapenos/stamina/stamina)
![License](https://img.shields.io/badge/license-MIT-blue.svg "License: MIT")

Stamina is an Akka serialization toolkit written specifically for use with Akka Persistence.
It has a strong focus on long-term viability of your persisted data so it provides support for **versioning** that data, **auto-migrating** that data at read time to be compatible with your current event and domain classes, and a **testkit** to make sure all older versions of your persisted data are still readable.

Do feel free to browse, comment, create issues, pull requests, etc. If you are interested in using Stamina, please feel welcome to join our chat room

[![Join the chat at https://gitter.im/scalapenos/stamina](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scalapenos/stamina)

We are still finishing up the last open ends before we release a public version but in the mean time Stamina (and some of its less fancy predecessors) has been running in production for many months now without any problems.


## Current status
Stamina is currently available in pre-release SNAPSHOT form. This means that we don't recommend using Stamina in production since the APIs could still change significantly and break your stuff.

To use the latest SNAPSHOT release, configure your SBT build like this:

```
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies += "com.scalapenos" %% "stamina-json" % "0.1.3"
```

[stamina-core](stamina-core) and [stamina-json](stamina-json) are pretty much ready and we are happy with the API that has evolved out of the many experiments we have done over the last year. We **are** looking at the new Akka 2.4.x event adapters to see whether integration with akka can be improved using them.

[stamina-testkit](stamina-testkit) is also mostly finished. It can be used to generate scalatest tests for your stamina persisters, keeping serialized older versions of your data around to make sure you don't accidentally break compatibility.

Our final task before releasing a public beta is to create a *stamina-sample-app* to show Stamina in action on a representative project.

## Adding Stamina to your project
Stamina is available via [Maven Central](https://search.maven.org/), simply add it to your SBT build:

```scala
libraryDependencies += "com.scalapenos" %% "stamina-json" % "0.1.2"
```

If you want to use a development snapshots, use the 
[Sonatype Snapshot Repository](https://oss.sonatype.org/content/repositories/snapshots/com/scalapenos/). Add the
following lines to your SBT build:
```scala
resolvers ++= Seq(
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies += "com.scalapenos" %% "stamina-json" % "0.1.2"
```

# Stamina in Detail
Stamina is an Akka serialization toolkit written specifically for use with Akka Persistence. Its main defining characteristics are:

- support for explicitly versioning serialized data. Stamina always stores a version number with the serialized data.
- support for explicitly migrating serialized data written for older versions to the latest version
- support for defining (de)serialization behavior in code. This includes (auto-)generating so-called *persisters* from Scala case classes and an API for specifying migration behaviors.
- decoupling from fully qualified class names (or randomly generated ids) as serialization keys. Instead, Stamina uses simple String keys to identify serialized types.
- support for multiple serialization libraries as implementation plugins, as long as they can be ported/adjusted/wrapped in order to support the above features.

The first (and currently only) implementation is based on spray-json. It supports migration from older versions using a very simple little DSL to pre-process the JSON AST based on the specific version being read before deserialization takes place. Here's an example:

```scala
// This example uses explicitly versioned case classes (i.e. the same
// domain class in three different versions with three different names)
// to more easily show how to deal with versions and migrations.
//
// Normally, of course, you would only need one case class,
// which would always represent the current version (V3 in this case).
import fommil.sjs.FamilyFormats._ // For this example's ease, auto-generated json formats are used here
import spray.json.lenses.JsonLenses._
import stamina.json._

// spray-json persister for V1.
// Essentially equivalent to any existing Akka serializer except
// for the simple API used to specify/generate them.
val v1CartCreatedPersister = persister[CartCreatedV1]("cart-created")

// spray-json persister for V2 but with support for migration
// of data writen in the V1 format.
val v2CartCreatedPersister = persister[CartCreatedV2, V2](
  "cart-created",
  from[V1]
    .to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
)

// spray-json persister for V3 but with support for migration
// of data writen in the V1 and V2 formats.
val v3CartCreatedPersister = persister[CartCreatedV3, V3](
  "cart-created",
  from[V1]
    .to[V2](_.update('cart / 'items / * / 'price ! set[Int](1000)))
    .to[V3](_.update('timestamp ! set[Long](System.currentTimeMillis)))
)
```

For these persisters to be actually used by the Akka serialization system, you will need to bundle them into an Akka
serializer and then register that serializer for your classes. To make that registration process a little simpler
Stamina comes with a marker trait called `Persistable`. Of course you can use your own marker traits instead.

```scala
class CartCreatedV3(...) extends Persistable
```

In the example below we create a subclass of `StaminaAkkaSerializer` and pass all our Persister instances into it. We
then register this serializer with Akka in our application.conf and bind it to all instances/subclasses of the
`Persistable` marker trait.

```scala
class WebshopSerializer extends StaminaAkkaSerializer(v3CartCreatedPersister, ...)
```

```
akka.actor {
  serializers {
    serializer  = "package.name.WebshopSerializer"
  }
  serialization-bindings {
    "stamina.Persistable" = serializer
  }
}
```

An example application and a testkit with support for regression
testing of serialized older versions are currently being worked on.

Support for Apache Avro and Scala's own pickling formats is pending.


## Why?
Stamina aims to be the serialization toolkit that should have come with Akka Persistence but didn't.

### So what's wrong with Akka Persistence?
Akka Persistence is an awesome library for implementing event stores and event-sourced persistent actors.

The problem arises from the fact that Akka Persistence reuses the standard Akka serialization system without adding any kind of support for data versioning or deserialization of older versions of persisted data.

This has lead to people having to maintain multiple older versions of their domain/event model in parallel or to somehow work around this limitation. This project has grown out of one such workaround.

The Akka serialization system was originally written to support remote actors and clustered actors. It is optimized for raw performance and low configuration overhead but it assumes that the code that serializes your data is the exact same code that deserializes it later. This is not necessarily the case in an event store, where data might have been serialized by an older iteration of your code.

An event store without some kind of versioning/migration system is not very useful in our opinion so we set out to provide these features in a way that is backwards compatible with Akka Persistence.

Some other problems with the existing Akka serialization system (and many other serialization libraries) are:

- coupling between the serialized data and a specific Java/Scala class or class name, usually even a fully qualified one. This creates a barrier to refactoring of the domain model, something that is very necessary to keep active code bases healthy and clean. Renaming and repackaging of such classes is no longer possible without losing backwards compatibility with your already persisted versions.

- no high-level API for configuring which classes get serialized how. The only available option is linking the fully qualified class name of a specific class (or superclass) to the fully qualified classname of a serializer. This allows for very little flexibility, no support for versioning or migrations, and leads to having to write lots of explicit, low-level serialization code.

Most of these problems arise from the simple fact that versioning and migration were never part of the design of such serialization systems.


## Goals / Approach

1. Stamina should support pluggable serialization implementations so people can reuse their existing formats and libraries.
2. Stamina should support code-level configuration options like type classes and macros so it can use all the power provided by the existing libraries.
3. Stamina should provide explicit support for versioning serialized data (i.e. data is always written together with a version number).
4. Stamina should provide support for decoupling from the fully qualified class name of serialized classes (i.e. data is always written together with a symbolic key),
5. Stamina should provide an easy to use API for deserializing older versions of stored classes (i.e. migrations).
6. Stamina should be highly customizable from code while requiring the least possible amount of boilerplate
7. Stamina should use the Scala type system to prevent common serialization, versioning, and migration problems.
