package stamina.testkit

import org.scalatest._
import events._

class EventRecordingReporter extends Reporter {
  private var eventList: List[Event] = List()

  def findResultEvent(testName: String): Option[Event] = {
    eventList.find {
      case evt: TestSucceeded if evt.testName == testName ⇒ true
      case evt: TestFailed if evt.testName == testName    ⇒ true
      case _                                              ⇒ false
    }
  }
  def eventsReceived = eventList.reverse

  def testSucceededEventsReceived: List[TestSucceeded] = {
    eventsReceived filter {
      case event: TestSucceeded ⇒ true
      case _                    ⇒ false
    } map {
      case event: TestSucceeded ⇒ event
      case _                    ⇒ throw new RuntimeException("should never happen")
    }
  }
  def infoProvidedEventsReceived: List[InfoProvided] = {
    eventsReceived filter {
      case event: InfoProvided ⇒ true
      case _                   ⇒ false
    } map {
      case event: InfoProvided ⇒ event
      case _                   ⇒ throw new RuntimeException("should never happen")
    }
  }
  def testPendingEventsReceived: List[TestPending] = {
    eventsReceived filter {
      case event: TestPending ⇒ true
      case _                  ⇒ false
    } map {
      case event: TestPending ⇒ event
      case _                  ⇒ throw new RuntimeException("should never happen")
    }
  }
  def testFailedEventsReceived: List[TestFailed] = {
    eventsReceived filter {
      case event: TestFailed ⇒ true
      case _                 ⇒ false
    } map {
      case event: TestFailed ⇒ event
      case _                 ⇒ throw new RuntimeException("should never happen")
    }
  }
  def testIgnoredEventsReceived: List[TestIgnored] = {
    eventsReceived filter {
      case event: TestIgnored ⇒ true
      case _                  ⇒ false
    } map {
      case event: TestIgnored ⇒ event
      case _                  ⇒ throw new RuntimeException("should never happen")
    }
  }
  def apply(event: Event) {
    eventList ::= event
  }
}
