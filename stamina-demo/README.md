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

So far we only know the name of the sender of a message. Let's say we want to add more information about the message, e.g. the time it was sent, the location of the sender at the time of sending and the device that was used. To incorporate this information we construct a new case class `Sender` and adapt the ``Message` to use it. 

```scala
case class Sender(name: String, location: String, device: String)

case class Message(text: String, timestamp: DateTime, sender: Sender)
```

Notice also that we renamed the attribute `from` to `sender`.

## Step 2 - Multi chatroom

## Step 3 - List of chatroom

## Step 3 - Accounts and their properties

## Step 4 - Visibility / private rooms
