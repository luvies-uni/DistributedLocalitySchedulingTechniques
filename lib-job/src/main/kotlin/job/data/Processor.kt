package job.data

import job.metrics.MetricsSender
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.lang.Thread.sleep

class Processor(
  config: ProcessorConfig,
  private val metricsSender: MetricsSender
) {
  private val logger = LoggerFactory.getLogger(javaClass)

  private val downloadTime = config.downloadTime
  private val processTime = config.processTime
  private val cacheTime = config.cacheTime

  private val cache = mutableMapOf<String, Long>()

  var cacheDropHandler: ((repo: String) -> Unit)? = null

  fun process(job: RepositoryJob) {
    // Handle cache.
    val curTime = currentTimeMillis()
    cache
      .filter { (repo, cached) ->
        repo != job.repository && cached + cacheTime < curTime
      }
      .forEach { (repo) ->
        cache.remove(repo)
        cacheDropHandler?.invoke(repo)
        logger.info("Removed repository {} from cache", repo)
      }

    if (job.repository !in cache) {
      // Simulate download.
      sleep(downloadTime)
      cache[job.repository] = curTime

      // Notify of cache miss
      metricsSender.cacheMiss(1)

      logger.info("Downloaded repository {} in {}ms", job.repository, downloadTime)
    } else {
      // Refresh cache time.
      cache[job.repository] = curTime
    }

    // Simulate process.
    sleep(processTime)
    logger.info("Processed task {} in {}ms", job.task, processTime)

    // Count the job as done.
    metricsSender.countJob(1)
  }

  fun isRepositoryCached(repo: String): Boolean = repo in cache
}

data class ProcessorConfig(
  val downloadTime: Long,
  val processTime: Long,
  val cacheTime: Long
)
