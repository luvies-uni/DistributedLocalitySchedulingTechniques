package job.impl.roundRobin.consumer

import job.broker.JobConsumer
import job.broker.shutdownWrapper
import job.consts.genericJobQueue
import job.data.Processor
import job.metrics.MetricsSender
import job.util.LongConfig
import job.util.Signal

fun main() {
  LongConfig.consumerWait()
  shutdownWrapper { sig ->
    runConsumer(
      sig,
      // Test config
//      "tcp://localhost:61616",
//      60_000
      // Long running config
      LongConfig.dockerActiveMQUri,
      LongConfig.cacheTime
    )
  }
}

fun runConsumer(sig: Signal, brokerUri: String, cacheTime: Long) {
  MetricsSender(brokerUri).use { metricsSender ->
    JobConsumer(brokerUri, metricsSender).use { consumer ->
      val processor = Processor(cacheTime, metricsSender)
      val seenJobs = mutableSetOf<String>()
      while (sig.run) {
        consumer.receiveJob(genericJobQueue, 1000) {
          !seenJobs.add(it.repository) || processor.isRepositoryCached(it.repository)
        }?.let { processor.process(it) }

        processor.handleCache()
      }
    }
  }
}
