package job.impl.roundRobin.consumer

import job.broker.Consumer
import job.broker.shutdownWrapper
import job.data.Processor
import job.data.ProcessorConfig
import job.util.Signal

fun main() {
  shutdownWrapper { sig ->
    runConsumer(
      sig,
      ProcessorConfig("tcp://localhost:61616", 5000, 1000, 60000)
    )
  }
}

fun runConsumer(sig: Signal, processorConfig: ProcessorConfig) {
  Consumer(processorConfig.brokerUri).use { consumer ->
    Processor(processorConfig).use { processor ->
      val seenJobs = mutableSetOf<String>()
      while (sig.run) {
        consumer.receive("jobs/generic", 1000) {
          !seenJobs.add(it.repository) || processor.isRepositoryCached(it.repository)
        }?.let { processor.process(it) }
      }
    }
  }
}
