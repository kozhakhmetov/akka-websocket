import akka.NotUsed
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import scala.io.StdIn

object Boot extends App {
  implicit val system = ActorSystem("example")
  implicit val materializer = ActorMaterializer()

  def greeter: Flow[Message, Message, Any] =
    Flow[Message].mapConcat {
      case tm: TextMessage =>
        TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }

  val route: Route = path("greeter") {
    handleWebSocketMessages(greeter)
  }
  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  import system.dispatcher
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ ⇒ system.terminate())
}

class ClientConnectionActor extends Actor {
  var connection: Option[ActorRef] = None

  val receive: Receive = {
    case ('income, a: ActorRef) ⇒ connection = Some(a); context.watch(a)
    case Terminated(a) if connection.contains(a) ⇒ connection = None; context.stop(self)
    case 'sinkclose ⇒ context.stop(self)

    case TextMessage.Strict(t) ⇒ connection.foreach(_ ! TextMessage.Strict(s"echo $t"))
    case _ ⇒ // ingone
  }

  override def preStart(): Unit = println("Actor created")

  override def postStop(): Unit = connection.foreach(context.stop)
}