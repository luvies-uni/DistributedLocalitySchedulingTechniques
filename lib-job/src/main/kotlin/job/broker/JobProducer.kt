package job.broker

import job.data.RepositoryJob
import job.metrics.queues.TimingQueues
import org.slf4j.LoggerFactory

class JobProducer(brokerUri: String) : Producer(brokerUri) {
  private val logger = LoggerFactory.getLogger(javaClass)

  fun startTiming(jobQuantity: Int) {
    createProducer(TimingQueues.start).use { producer ->
      val message = session.createTextMessage(jobQuantity.toString())
      producer.send(message)

      logger.info("Started timing with {} items", jobQuantity)
    }
  }

  fun sendJob(queue: String, job: RepositoryJob) {
    send(queue, job.stringify())
  }
}
