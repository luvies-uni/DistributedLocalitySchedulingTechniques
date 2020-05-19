package job.data

import job.metrics.MetricsSender
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.lang.Thread.sleep

class Processor(
  private val cacheTime: Long,
  private val metricsSender: MetricsSender
) {
  private val logger = LoggerFactory.getLogger(javaClass)

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
      sleep(job.downloadTime)
      cache[job.repository] = curTime

      // Notify of cache miss
      metricsSender.cacheMiss(1)

      logger.info("Downloaded repository {} in {}ms", job.repository, job.downloadTime)
    } else {
      // Refresh cache time.
      cache[job.repository] = curTime
    }

    // Simulate process.
    sleep(job.processTime)
    logger.info("Processed task {} in {}ms", job.task, job.processTime)

    // Count the job as done.
    metricsSender.countJob(1)
  }

  fun isRepositoryCached(repo: String): Boolean = repo in cache
}
