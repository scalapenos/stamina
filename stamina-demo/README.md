# stamina-demo

This project aims to provide a simple demo on how to use stamina to migrate akka-persisted events to their current version. In the demo we will build a simple chat application which logs sent messages starting with a minimalistic version and gradually extending its features. 

## Set up your project to use stamina

The first step would be to depend on the stamina library and configure 

## Step 1 - Simple chat in single chatroom

```scala
  case class Message(text: String, from: String)

  object ChatRoom {
    def props = Props(new ChatRoom)
    val name = "chatroom"
  }

  class ChatRoom extends PersistentActor {
    sealed trait Command
    case class Join extends Command
    case class SendMessage(msg: Message) extends Command

    sealed trait Event
    case class MessageSent(msg: Message) extends Event
    case class Joined(history: Seq[Message]) extends Event

    var messages: Seq[Message]
    var users: Set[ActorRef]

    def receiveCommand: Receive = {
      case Join =>
        users = users + sender()
        sender() ! Joined(messages)
      case SendMessage(text, from) =>
        persist(MessageSent(text, from)) { event =>
          updateState(event)
        }
    }

    def receiveRecover: Receive = {
      case e: Event => updateState(e)
    }

    private def updateState(event: Event) = event match {
      case MessageSent(msg) => 
        messages = messages +: msg
        users.foreach(_ ! msg)
    }
  } 

```

## Step 2 - Extend sender information

So far we only know the name of the sender of a message. Let's say we want to add more information about the message, e.g. the time it was sent and the location of the sender at the time of sending. To incorporate this information we construct a new case class `Sender` and adapt the ``Message` to use it. 

```scala
case class Sender(name: String, location: String)

case class Message(text: String, timestamp: DateTime, sender: Sender)
```

Notice also that we renamed the attribute `from` to `sender`.

## Step 3 - Limit the amount of users in the chatroom

Chatting with too many users can be chaotic. Therefore we want to limit the amount of users that are allowed at one time in the chatroom. This limit can be set by sending a `SetUserLimit` to the `ChatRoom`.

```scala
class ChatRoom extends PersistentActor {
  ...
  case class SetUserLimit(limit: Int) extends Command

  case class UserLimitSet(limit: Int) extends Event

  var limit: Option[Int] = None

  def receiveCommand: Receive = {
    case Join if limit.fold(false)(_ == users.size) =>
      sender() ! UserLimitReached
    case SetUserLimit(limit) =>
      persist(UserLimitSet(limit)) { event =>
        updateState(event)
      }
    ...
  }

  private def updateState(event: Event) = event match {
    ...
    case UserLimitSet(lim) => limit = Some(lim)
    ...
  }
}
```

## Step 4 Privileged users

Some users have special privileges and are always allowed in the room, even if the user limit is reached. To achieve this functionality the `SetUserLimit` event is extended and renamed to a more general `SetConfiguration` case class. Next to setting the limit the administrator is allowed to pass a list of privileged users. Note that in the previous version there would always be a limit once one was set. By using an `Option[Int]` in the command and corresponding event, we allow the removal of a maximum number of users in the chatroom.

```scala
class ChatRoom extends PersistentActor {

  case class SetConfiguration(limit: Option[Int], privilegedUsers: Set[String]) extends Command

  case class ConfigurationSet(limit: Option[Int], privilegedUsers: Set[String]) extends Event

  var limit: Option[Int] = None
  var priviligedUsers: Set[String] = Set.empty

  def receiveCommand: Receive = {
    case Join(username) if privilegedUsers.contains(username) =>
       join(username)
    case Join if limit.fold(false)(_ == users.size) =>
      sender() ! UserLimitReached
    case SetConfiguration(limit, privilegedUsers) =>
      persist(ConfigurationSet(limit, privilegedUsers)) { event =>
        updateState(event)
      }
  }

  private def updateState(event: Event) = event match {
    ...
    case ConfigurationSet(limit, privilegedUsers) =>
      this.limit = limit
      this.privilegedUsers = privilegedUsers
    ...
  }
}
```

## Step 5 - ....
