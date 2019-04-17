package WS.Test

import com.rabbitmq.client.{DeliverCallback, ConnectionFactory}
import scala.util.parsing.json


import com.rabbitmq.client.{ConnectionFactory, DeliverCallback}
import spray.json._
import DefaultJsonProtocol._
import WS.Test.Server
import models.Message

trait MyJsonProtocol extends DefaultJsonProtocol {
  implicit val messageFormat = jsonFormat2(Message)
}

object Receiver extends MyJsonProtocol {

  private val QUEUE_NAME = "messages"

  def main() {
    val factory = new ConnectionFactory()
    factory.setHost("localhost")
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(QUEUE_NAME, true, false, false, null)
    println(" [*] Waiting for messages. To exit press CTRL+C")
    val deliverCallback: DeliverCallback = (_, delivery) => {
      val messageString = new String(delivery.getBody, "UTF-8")
      val message = messageString.parseJson.convertTo[Message]
      println(" [x] Received  with userID: " + message.userID + " message: " + message.message)
      try {
        Server.messageSender ! message
      } finally {
        println(" Done")
        channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
      }

    }
    channel.basicConsume(QUEUE_NAME, false, deliverCallback, _ => { })
  }
}