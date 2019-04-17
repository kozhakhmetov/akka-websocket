package rabbitmq.HelloWorld

import com.rabbitmq.client.{ConnectionFactory, DeliverCallback}
import spray.json._
import DefaultJsonProtocol._
import WS.Test.Server



object Recv {

  private val QUEUE_NAME = "messages"

  def main(argv: Array[String]) {
    val factory = new ConnectionFactory()
    factory.setHost("localhost")
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(QUEUE_NAME, true, false, false, null)
    println(" [*] Waiting for messages. To exit press CTRL+C")
    val deliverCallback: DeliverCallback = (_, delivery) => {
      val message = new String(delivery.getBody, "UTF-8")

      println(" [x] Received '" + message + "'")
    }
    channel.basicConsume(QUEUE_NAME, true, deliverCallback, _ => { })
  }
}