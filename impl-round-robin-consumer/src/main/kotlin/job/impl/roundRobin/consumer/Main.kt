package job.impl.roundRobin.consumer

import job.broker.JobConsumer
import job.broker.shutdownWrapper
import job.consts.genericJobQueue
import job.data.Processor
import job.data.ProcessorConfig
import job.metrics.MetricsSender
import job.util.Signal

fun main() {
  shutdownWrapper { sig ->
    runConsumer(
      sig,
      "tcp://localhost:61616",
      ProcessorConfig(5000, 1000, 60000)
    )
  }
}

fun runConsumer(sig: Signal, brokerUri: String, processorConfig: ProcessorConfig) {
  MetricsSender(brokerUri).use { metricsSender ->
    JobConsumer(brokerUri, metricsSender).use { consumer ->
      val processor = Processor(processorConfig, metricsSender)
      val seenJobs = mutableSetOf<String>()
      while (sig.run) {
        consumer.receiveJob(genericJobQueue, 1000) {
          !seenJobs.add(it.repository) || processor.isRepositoryCached(it.repository)
        }?.let { processor.process(it) }
      }
    }
  }
}
