package job.consumer.roundRobin

import job.broker.Consumer
import job.broker.shutdownWrapper
import job.data.Processor

fun main() {
  shutdownWrapper { sig ->
    Consumer("tcp://localhost:61616").use { consumer ->
      val processor = Processor(5000, 1000, 60000)
      val seenJobs = mutableSetOf<String>()
      while (sig.run) {
        consumer.receive("jobs/generic", 1000) {
          !seenJobs.add(it.repository) || processor.isRepositoryCached(it.repository)
        }?.let { processor.process(it) }
      }
    }
  }
}
