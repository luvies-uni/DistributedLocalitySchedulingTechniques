package job.generator.roundRobin

import job.broker.Producer
import job.broker.shutdownWrapper
import job.data.Generator
import java.lang.Thread.sleep

fun main() {
  shutdownWrapper { sig ->
    Producer("tcp://localhost:61616").use { producer ->
      val generator = Generator(10)
      val totalJobs = 10
      producer.startTiming(totalJobs)

      var doneJobs = 0
      while (sig.run && doneJobs < totalJobs) {
        producer.send("jobs/generic", generator.nextJob())
        doneJobs++

        // Allow process to close during wait time.
        sleep(1000)
      }
    }
  }
}
