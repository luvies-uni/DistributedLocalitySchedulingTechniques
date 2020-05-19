package job.internalScheduler

import job.impl.roundRobin.consumer.runConsumer
import job.impl.roundRobin.generator.runGenerator

fun roundRobin() {
  val metricsResult = testImpl(
    "roundRobin",
    { sig, config -> runConsumer(sig, config.brokerUri, config.cacheTime) },
    { sig, config ->
      runGenerator(sig, config.brokerUri, config.repoCount, config.totalJobs, config.produceDelay)
    }
  )

  println("Round robin metrics: $metricsResult")
}
