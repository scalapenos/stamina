# stamina-testkit

*stamina-testkit* contains utilities for generating tests for persisters based on example events.

## Usage

Currently only scalatest is supported. Tests can be generated based on example events like this:

```scala
import stamina._
import stamina.testkit._

import BasketActor._

import common.DateTimeSupport._

class BasketActorAkkaSerializationSpec extends UnitSpec with ScalaTestKit {
  val persisters = Persisters(BasketActorAkkaSerialization.persisters)

  val timestamp = "2015-01-01T12:34:56.000Z".toDateTime
  val code = "EXAMPLE_CODE"

  "The BasketActor akka serialization" should {
    persisters.generateTestsFor(
      sample(CouponClaimedEvent(code, timestamp)),
      sample(BasketDeletedEvent(timestamp)),
      sample(BasketInitializedEvent(timestamp)))
  }

}
```

This will generate tests like this:

```
(master) root: testOnly *BasketActorAkka*
[info] Compiling 1 Scala source to /home/aengelen/xebia/stamina-example/target/scala-2.11/classes...
[info] BasketActorAkkaSerializationSpec:
[info] The BasketActor akka serialization
[info] - should persist and unpersist CouponClaimedEvent
[info] - should deserialize the stored serialized form of CouponClaimedEvent version 1
[info] - should persist and unpersist BasketDeletedEvent
[info] - should deserialize the stored serialized form of BasketDeletedEvent version 1
[info] - should persist and unpersist BasketInitializedEvent
[info] - should deserialize the stored serialized form of BasketInitializedEvent version 1
[info] Run completed in 255 milliseconds.
[info] Total number of tests run: 6
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 6, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 1 s, completed Apr 28, 2015 11:46:55 PM
(master) root: 
```

These tests will not only test that the persisters can currently perform a serialization
roundtrip, but they will also check that previously stored serialized events can be restored.

The first time the test is ran for a new version of an event, a serialized form of the example
event is generated on disk. This file should be checked into version control along with the test.
