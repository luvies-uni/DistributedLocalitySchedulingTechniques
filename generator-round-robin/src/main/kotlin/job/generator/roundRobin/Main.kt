package job.generator.roundRobin

import job.broker.Producer
import job.broker.shutdownWrapper
import job.data.Generator
import java.lang.Thread.sleep

fun main() {
  shutdownWrapper { sig ->
    Producer("tcp://localhost:61616").use { producer ->
      val generator = Generator(10)
      while (sig.run) {
        producer.send("jobs/generic", generator.nextJob())

        // Allow process to close during wait time.
        for (i in 1..10) {
          sleep(1000)
          if (!sig.run) {
            break
          }
        }
      }
    }
  }
}
