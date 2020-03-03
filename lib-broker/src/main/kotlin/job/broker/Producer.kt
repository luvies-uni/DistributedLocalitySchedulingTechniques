package job.broker

import org.slf4j.LoggerFactory
import javax.jms.DeliveryMode

class Producer(brokerUri: String) : ActiveMQConnection(brokerUri) {
  private val logger = LoggerFactory.getLogger(this.javaClass)

  fun send(queue: String, content: String) {
    // Create the destination (Topic or Queue)
    val destination = session.createQueue(queue)

    // Create a MessageProducer from the Session to the Topic or Queue
    val producer = session.createProducer(destination)
    producer.deliveryMode = DeliveryMode.NON_PERSISTENT

    val message = session.createTextMessage(content)
    producer.send(message)

    producer.close()

    logger.info("Sent message to $queue")
  }
}
