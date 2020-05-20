package job.data

import job.metrics.MetricsSender
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.lang.Thread.sleep
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Processor(
  private val cacheTime: Long,
  private val metricsSender: MetricsSender
) {
  private val logger = LoggerFactory.getLogger(javaClass)

  private val cache = mutableMapOf<String, Long>()
  private val cacheLock = ReentrantLock()

  var cacheDropHandler: ((repo: String) -> Unit)? = null

  fun process(job: RepositoryJob) {
    // Handle cache.
    handleCache()

    val curTime = currentTimeMillis()
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

  fun handleCache() {
    cacheLock.withLock {
      val curTime = currentTimeMillis()
      cache
        .filter { (_, cached) ->
          cached + cacheTime < curTime
        }
        .forEach { (repo) ->
          cache.remove(repo)
          cacheDropHandler?.invoke(repo)
          logger.info("Removed repository {} from cache", repo)
        }
    }
  }

  fun isRepositoryCached(repo: String): Boolean {
    handleCache()
    return repo in cache
  }
}
