package job.impl.roundRobin.generator

import job.broker.JobProducer
import job.broker.shutdownWrapper
import job.data.Generator
import job.util.Signal
import java.lang.Thread.sleep

fun main() {
  shutdownWrapper { sig ->
    runGenerator(sig, "tcp://localhost:61616", 10, 10, 1000)
  }
}

fun runGenerator(sig: Signal, brokerUri: String, repoCount: Int, totalJobs: Int, produceDelay: Long) {
  JobProducer(brokerUri).use { producer ->
    val generator = Generator(repoCount)
    producer.startTiming(totalJobs)

    var doneJobs = 0
    while (sig.run && doneJobs < totalJobs) {
      producer.sendJob("jobs/generic", generator.nextJob())
      doneJobs++

      // Allow process to close during wait time.
      sleep(produceDelay)
    }
  }
}
