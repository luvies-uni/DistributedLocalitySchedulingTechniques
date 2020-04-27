package job.data

import job.broker.ActiveMQConn
import job.broker.JmsProducer
import job.broker.timingCountQueueName
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.lang.Thread.sleep

class Processor(
  brokerUri: String,
  private val downloadTime: Long,
  private val processTime: Long,
  private val cacheTime: Long
) : ActiveMQConn(brokerUri) {
  private val logger = LoggerFactory.getLogger(javaClass)

  private val cache = mutableMapOf<String, Long>()

  fun process(job: RepositoryJob) {
    // Handle cache.
    val curTime = currentTimeMillis()
    cache.filter { (repo, cached) ->
      repo != job.repository && cached + cacheTime < curTime
    }.forEach { (repo) ->
      cache.remove(repo)
      logger.info("Removed repository {} from cache", repo)
    }

    if (job.repository !in cache) {
      // Simulate download.
      sleep(downloadTime)
      cache[job.repository] = curTime
      logger.info("Downloaded repository {} in {}ms", job.repository, downloadTime)
    } else {
      // Refresh cache time.
      cache[job.repository] = curTime
    }

    // Simulate process.
    sleep(processTime)
    logger.info("Processed task {} in {}ms", job.task, processTime)

    // Count the job as done.
    countJob()
  }

  private fun countJob() {
    val destination = session.createQueue(timingCountQueueName)

    JmsProducer(session.createProducer(destination)).use { producer ->
      val message = session.createTextMessage("1")
      producer.send(message)

      logger.info("Counted 1 job")
    }
  }

  fun isRepositoryCached(repo: String): Boolean = repo in cache
}
