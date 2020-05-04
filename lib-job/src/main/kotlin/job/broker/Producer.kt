package job.broker

import org.slf4j.LoggerFactory
import javax.jms.DeliveryMode
import javax.jms.MessageProducer

open class Producer(brokerUri: String) : ActiveMQConn(brokerUri) {
  private val logger = LoggerFactory.getLogger(javaClass)

  fun send(queue: String, text: String) {
    createProducer(queue).use { producer ->
      producer.deliveryMode = DeliveryMode.NON_PERSISTENT

      val message = session.createTextMessage(text)
      producer.send(message)

      logger.info("Sent {} to {}", text, queue)
    }
  }

  internal fun createProducer(queue: String): JmsProducer {
    return JmsProducer.create(this, queue)
  }
}

internal class JmsProducer(p: MessageProducer) : MessageProducer by p, AutoCloseable {
  companion object {
    @JvmStatic
    fun create(conn: ActiveMQConn, queue: String): JmsProducer {
      // Create the destination (Topic or Queue)
      val destination = conn.session.createQueue(queue)

      // Create a MessageProducer from the Session to the Topic or Queue
      return JmsProducer(conn.session.createProducer(destination))
    }
  }
}
