package job.metrics.queues

import job.consts.metricsQueueBase

class CacheQueues {
  companion object {
    const val misses = "${metricsQueueBase}/cache/misses"
  }
}
