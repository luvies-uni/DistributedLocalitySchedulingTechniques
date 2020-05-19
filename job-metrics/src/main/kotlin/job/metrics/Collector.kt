package job.metrics

import job.broker.Consumer
import job.metrics.queues.CacheQueues
import job.metrics.queues.JobQueues
import job.metrics.queues.TimingQueues
import job.util.Signal
import org.slf4j.LoggerFactory

class Collector(brokerUri: String) : Consumer(brokerUri, null) {
  private val logger = LoggerFactory.getLogger(javaClass)

  private val timingLock = Signal(false)

  private var totalJobsProcessTime: Long = -1
  private var cacheMisses: Long = 0
  private var jobRejectionCount: Long = 0

  val results
    get() = MetricsResult(totalJobsProcessTime, cacheMisses, jobRejectionCount)

  fun startCollection() {
    startJobProcessTimeCollection()
    startCacheMissesCollection()
    startRejectionCountCollection()
  }

  fun startJobProcessTimeCollection() {
    logger.info("Waiting for starting signal")
    timingLock.start()

    startListen(TimingQueues.start) { it.toInt() }() { totalJobs, _ ->
      logger.info("Started timing with {} jobs", totalJobs)
      stopListen(TimingQueues.start)

      var currentJobs = 0
      val start = System.currentTimeMillis()

      startListen(TimingQueues.count) { it.toInt() }() { count, _ ->
        currentJobs += count
        logger.debug("Current jobs complete: {}", currentJobs)

        if (currentJobs >= totalJobs) {
          totalJobsProcessTime = System.currentTimeMillis() - start
          stopListen(TimingQueues.count)
          timingLock.exit()
          logger.info("All jobs completed after {}ms", totalJobsProcessTime)
        }
      }
    }
  }

  fun startCacheMissesCollection() {
    logger.info("Started collection of cache misses")

    startListen(CacheQueues.misses) { it.toInt() }() { totalMisses, _ ->
      logger.debug("Received {} cache misses", totalMisses)

      cacheMisses += totalMisses
    }
  }

  fun startRejectionCountCollection() {
    logger.info("Started collection of job rejections")

    startListen(JobQueues.rejections) { it.toInt() }() { totalRejections, _ ->
      logger.debug("Received {} job rejections", totalRejections)

      jobRejectionCount += totalRejections
    }
  }

  fun waitForJobProcessTimeCollection(sig: Signal) {
    while (sig.run && timingLock.waitForExit(1000)) {
    }
  }

  override fun close() {
    super.close()
    timingLock.exit()
  }
}

data class MetricsResult(
  val batchCompletionTime: Long,
  val cacheMisses: Long,
  val jobRejectionCount: Long
) {
  override fun toString(): String {
    return "MetricsResult(batch completion time: ${batchCompletionTime}ms, cache misses: $cacheMisses, job rejection count: $jobRejectionCount)"
  }
}
