package WS.Test

import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, UpgradeToWebSocket}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer


import scala.io.StdIn

object Server extends App {


  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()

  val interface = "localhost"
  val port = 8080

  import akka.http.scaladsl.server.Directives._

  val messageSender = actorSystem.actorOf(MessageSender.props())

  Receiver.main()

  def echoService(id: Long): Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(_) => TextMessage("ECHO: " + id)
    case _ => TextMessage("Message type unsupported")
  }

  def flow(id: Long): Flow[Message, Message, Any] = {
    val client = actorSystem.actorOf(Props(classOf[ClientConnectionActor]))
    messageSender ! MessageSender.NewConnection(id, client)
    val in = Sink.actorRef(client, 'sinkclose)
    val out = Source.actorRef(8, OverflowStrategy.fail).mapMaterializedValue { a ⇒
      client ! ('income → a)
      a
    }
    Flow.fromSinkAndSource(in, out)
  }

  val route = get {
    pathEndOrSingleSlash {
      complete("Welcome to websocket server")
    }
  } ~
    path("ws-echo" / LongNumber) { id =>
      get {
        handleWebSocketMessages(flow(id))
      }
    }


  val binding = Http().bindAndHandle(route, interface, port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()

  import actorSystem.dispatcher

  binding.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  println("Server is down...")

}