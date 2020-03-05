package job.consumer.roundRobin

import job.broker.Consumer
import job.broker.shutdownWrapper
import job.data.Processor

fun main() {
  shutdownWrapper { sig ->
    Consumer("tcp://localhost:61616").use { consumer ->
      val processor = Processor(5000, 1000)
      while (sig.run) {
        val job = consumer.receive("jobs/generic", timeout = 1000)
        if (job != null) {
          processor.process(job)
        }
      }
    }
  }
}
