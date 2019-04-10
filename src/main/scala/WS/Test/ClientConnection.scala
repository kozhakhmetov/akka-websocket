package WS.Test

import akka.actor.{Actor, ActorRef, Terminated}
import akka.http.scaladsl.model.ws.TextMessage

class ClientConnectionActor extends Actor {
  var connection: Option[ActorRef] = None

  val receive: Receive = {
    case ('income, a: ActorRef) ⇒ connection = Some(a); context.watch(a)
    case Terminated(a) if connection.contains(a) ⇒ connection = None; context.stop(self)
    case 'sinkclose ⇒ context.stop(self)

    case TextMessage.Strict(t) ⇒ connection.foreach(_ ! TextMessage.Strict(s"echo $t"))
    case _ ⇒ // ingone
  }

  override def preStart(): Unit = println("ClientConnectionActor created")
  override def postStop(): Unit = {
    connection.foreach(context.stop)
    println("ClientConnectionActor destroyed")
  }
}