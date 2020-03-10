package job.data

import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.lang.Thread.sleep

class Processor(
  private val downloadTime: Long,
  private val processTime: Long,
  private val cacheTime: Long
) {
  private val logger = LoggerFactory.getLogger(this.javaClass)

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
  }

  fun isRepositoryCached(repo: String): Boolean = repo in cache
}
