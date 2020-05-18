package job.data

import job.broker.ActiveMQConn
import job.broker.JmsProducer
import job.metrics.queues.TimingQueues
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.lang.Thread.sleep

class Processor(
  config: ProcessorConfig
) : ActiveMQConn(config.brokerUri) {
  private val logger = LoggerFactory.getLogger(javaClass)

  private val downloadTime = config.downloadTime
  private val processTime = config.processTime
  private val cacheTime = config.cacheTime

  private val cache = mutableMapOf<String, Long>()

  var cacheDropHandler: ((repo: String) -> Unit)? = null

  fun process(job: RepositoryJob) {
    // Handle cache.
    val curTime = currentTimeMillis()
    cache.filter { (repo, cached) ->
      repo != job.repository && cached + cacheTime < curTime
    }.forEach { (repo) ->
      cache.remove(repo)
      cacheDropHandler?.invoke(repo)
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
    val destination = session.createQueue(TimingQueues.count)

    JmsProducer(session.createProducer(destination)).use { producer ->
      val message = session.createTextMessage("1")
      producer.send(message)

      logger.info("Counted 1 job")
    }
  }

  fun isRepositoryCached(repo: String): Boolean = repo in cache
}

data class ProcessorConfig(
  val brokerUri: String,
  val downloadTime: Long,
  val processTime: Long,
  val cacheTime: Long
)
