package job.broker

import job.data.RepositoryJob
import job.metrics.queues.TimingQueues
import org.slf4j.LoggerFactory
import javax.jms.DeliveryMode
import javax.jms.MessageProducer

class Producer(brokerUri: String) : ActiveMQConn(brokerUri) {
  private val logger = LoggerFactory.getLogger(javaClass)

  fun startTiming(jobQuantity: Int) {
    val destination = session.createQueue(TimingQueues.start)

    JmsProducer(session.createProducer(destination)).use { producer ->
      val message = session.createTextMessage(jobQuantity.toString())
      producer.send(message)

      logger.info("Started timing with {} items", jobQuantity)
    }
  }

  fun send(queue: String, job: RepositoryJob) {
    // Create the destination (Topic or Queue)
    val destination = session.createQueue(queue)

    // Create a MessageProducer from the Session to the Topic or Queue
    JmsProducer(session.createProducer(destination)).use { producer ->
      producer.deliveryMode = DeliveryMode.NON_PERSISTENT

      val message = session.createTextMessage(job.stringify())
      producer.send(message)

      logger.info("Sent {} to {}", job, queue)
    }
  }
}

class JmsProducer(p: MessageProducer) : MessageProducer by p, AutoCloseable
