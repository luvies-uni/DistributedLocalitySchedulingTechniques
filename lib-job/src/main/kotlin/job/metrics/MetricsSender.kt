package job.metrics

import job.broker.ActiveMQConn
import job.broker.JmsProducer
import job.metrics.queues.CacheQueues
import job.metrics.queues.JobQueues
import job.metrics.queues.TimingQueues
import org.slf4j.LoggerFactory

class MetricsSender(brokerUri: String) : ActiveMQConn(brokerUri) {
  private val logger = LoggerFactory.getLogger(javaClass)

  fun countJob(count: Long) {
    sendMetricsLong(TimingQueues.count, count)
  }

  fun cacheMiss(count: Long) {
    sendMetricsLong(CacheQueues.misses, count)
  }

  fun jobRejection(count: Long) {
    sendMetricsLong(JobQueues.rejections, count)
  }

  private fun sendMetricsLong(queue: String, num: Long) {
    val destination = session.createQueue(queue)

    JmsProducer(session.createProducer(destination)).use { producer ->
      val message = session.createTextMessage(num.toString())
      producer.send(message)

      logger.info("Sent {} to metrics queue {}", num, queue)
    }
  }
}
