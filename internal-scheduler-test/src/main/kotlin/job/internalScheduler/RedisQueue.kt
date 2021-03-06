package job.internalScheduler

import job.impl.redisQueue.consumer.runConsumer
import job.impl.redisQueue.generator.runGenerator
import job.internalScheduler.services.RedisService
import job.metrics.MetricsResult

fun redisQueue(): MetricsResult? {
  val redisUri = "localhost"

  return RedisService().use { redisService ->
    redisService.reset()

    val metricsResult = testImpl(
      "redisQueue",
      { sig, config -> runConsumer(sig, config.brokerUri, config.idleTime, redisUri, config.cacheTime) },
      { sig, config ->
        runGenerator(sig, redisUri, config.brokerUri, config.repoCount, config.totalJobs, config.produceDelay)
      }
    )
    println("Redis queue metrics: $metricsResult")

    metricsResult
  }
}
