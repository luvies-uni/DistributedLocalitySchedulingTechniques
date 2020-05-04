package job.metrics

import job.broker.ActiveMQConn
import job.broker.JmsConsumer
import job.metrics.queues.TimingQueues
import job.util.Signal
import org.slf4j.LoggerFactory
import javax.jms.Message
import javax.jms.TextMessage
import kotlin.system.measureTimeMillis

class Timer(brokerUri: String) : ActiveMQConn(brokerUri) {
  private val logger = LoggerFactory.getLogger(javaClass)

  fun timeJobs(sig: Signal): Long {
    logger.info("Waiting for starting signal")

    val totalJobs = createConsumer(TimingQueues.start).use { receive(it, sig) } ?: return -1
    logger.info("Started timing with {} jobs", totalJobs)
    var currentJobs = 0

    val jobProcessTime = createConsumer(TimingQueues.count).use { consumer ->
      measureTimeMillis {
        while (currentJobs < totalJobs) {
          currentJobs += receive(consumer, sig) ?: return@measureTimeMillis
          logger.debug("Current jobs complete: {}", currentJobs)
        }
      }
    }

    if (currentJobs < totalJobs) {
      logger.info("Stopped timing after {}ms", jobProcessTime)
    } else {
      logger.info("All jobs completed after {}ms", jobProcessTime)
    }

    return jobProcessTime
  }

  private fun createConsumer(queue: String): JmsConsumer {
    return JmsConsumer.create(this, queue)
  }

  private fun receive(consumer: JmsConsumer, signal: Signal): Int? {
    while (signal.run) {
      val message: Message? = consumer.receive(1000)
      if (message != null && message is TextMessage) {
        message.acknowledge()
        return message.text.toInt()
      }
    }

    return null
  }
}
