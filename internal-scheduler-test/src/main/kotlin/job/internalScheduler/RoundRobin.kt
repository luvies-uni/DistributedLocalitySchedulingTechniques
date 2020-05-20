package job.internalScheduler

import job.impl.roundRobin.consumer.runConsumer
import job.impl.roundRobin.generator.runGenerator
import job.metrics.MetricsResult

fun roundRobin(): MetricsResult? {
  val metricsResult = testImpl(
    "roundRobin",
    { sig, config -> runConsumer(sig, config.brokerUri, config.cacheTime) },
    { sig, config ->
      runGenerator(sig, config.brokerUri, config.repoCount, config.totalJobs, config.produceDelay)
    }
  )

  println("Round robin metrics: $metricsResult")

  return metricsResult
}
