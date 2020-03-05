package job.data

import org.slf4j.LoggerFactory
import java.lang.Thread.sleep

class Processor(private val downloadTime: Long, private val processTime: Long) {
  private val logger = LoggerFactory.getLogger(this.javaClass)

  fun process(job: RepositoryJob) {
    // Currently no cache sim.
    // Simulate download.
    sleep(downloadTime)
    logger.info("Downloaded repository {} in {}ms", job.repository, downloadTime)

    // Simulate process.
    sleep(processTime)
    logger.info("Processed task {} in {}ms", job.task, processTime)
  }
}
