package job.consumer.roundRobin

import job.broker.Consumer
import job.broker.Signal
import job.broker.shutdownWrapper
import job.data.Processor

fun main() {
  shutdownWrapper { sig ->
    runConsumer(sig, "tcp://localhost:61616", 5000, 1000, 60000)
  }
}

fun runConsumer(sig: Signal, brokerUri: String, downloadTime: Long, processTime: Long, cacheTime: Long) {
  Consumer(brokerUri).use { consumer ->
    Processor(brokerUri, downloadTime, processTime, cacheTime).use { processor ->
      val seenJobs = mutableSetOf<String>()
      while (sig.run) {
        consumer.receive("jobs/generic", 1000) {
          !seenJobs.add(it.repository) || processor.isRepositoryCached(it.repository)
        }?.let { processor.process(it) }
      }
    }
  }
}
