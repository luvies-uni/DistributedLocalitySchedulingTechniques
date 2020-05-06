package job.internalScheduler

import job.impl.roundRobin.consumer.runConsumer
import job.impl.roundRobin.generator.runGenerator

fun roundRobin() {
  val processTime = testImpl(
    "roundRobin",
    { sig, config -> runConsumer(sig, config.processorConfig) },
    { sig, config ->
      runGenerator(sig, config.brokerUri, config.repoCount, config.totalJobs, config.produceDelay)
    }
  )

  println("Round robin took ${processTime}ms")
}
