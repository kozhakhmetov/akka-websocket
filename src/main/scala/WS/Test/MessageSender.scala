package WS.Test


import WS.Test.MessageSender.NewConnection
import WS.Test.models.Message
import akka.actor.{Actor, ActorRef, Props, Terminated}
import akka.http.scaladsl.model.ws.TextMessage

object MessageSender {
  def props() = Props(new MessageSender())

  case class NewConnection(id: Long, connectionActor: ActorRef)
}

class MessageSender() extends Actor{

  var userConnections = Map.empty[Long, ActorRef]

  override def receive: Receive = {
    case NewConnection(id,connectionActor) => userConnections = userConnections + (id -> connectionActor)
      println(s"Added in userConnections with id $id")
    case Message(id, message) =>
      println(s"Received message: $message to user: $id")
      userConnections.get(id) match {
        case Some(connectionRef) => sendMessage(connectionRef, message)
        case None => println(s"No user connection with id: $id")
      }

  }
  def sendMessage(target: ActorRef, message: String) = {
    target ! message
  }
}
