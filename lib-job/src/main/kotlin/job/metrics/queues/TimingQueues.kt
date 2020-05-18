package job.metrics.queues

import job.consts.metricsQueueBase

class TimingQueues {
  companion object {
    private const val base = "${metricsQueueBase}/timing"
    
    const val start = "${base}/start"
    const val count = "${base}/count"
  }
}
