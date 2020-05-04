package job.metrics

import job.broker.Consumer
import job.metrics.queues.TimingQueues
import job.util.Signal
import org.slf4j.LoggerFactory

class Collector(brokerUri: String) : Consumer(brokerUri) {
  private val logger = LoggerFactory.getLogger(javaClass)

  private val timingLock = Signal(false)

  var totalJobsProcessTime: Long? = null

  fun startJobTiming() {
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

  fun waitForJobTiming(sig: Signal) {
    while (sig.run && timingLock.waitForExit(1000)) {
    }
  }

  override fun close() {
    super.close()
    timingLock.exit()
  }
}
