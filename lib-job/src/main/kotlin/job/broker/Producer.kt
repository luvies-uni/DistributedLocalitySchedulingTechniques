package job.broker

import job.data.RepositoryJob
import org.slf4j.LoggerFactory
import javax.jms.DeliveryMode

class Producer(brokerUri: String) : ActiveMQConnection(brokerUri) {
  private val logger = LoggerFactory.getLogger(this.javaClass)

  fun send(queue: String, job: RepositoryJob) {
    // Create the destination (Topic or Queue)
    val destination = session.createQueue(queue)

    // Create a MessageProducer from the Session to the Topic or Queue
    val producer = session.createProducer(destination)

    try {
      producer.deliveryMode = DeliveryMode.NON_PERSISTENT

      val message = session.createTextMessage(job.stringify())
      producer.send(message)

      logger.info("Sent {} to {}", job, queue)
    } finally {
      producer.close()
    }
  }
}
