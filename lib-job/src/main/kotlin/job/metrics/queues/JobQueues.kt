package job.metrics.queues

import job.consts.metricsQueueBase

class JobQueues {
  companion object {
    const val rejections = "${metricsQueueBase}/jobs/rejections"
  }
}
