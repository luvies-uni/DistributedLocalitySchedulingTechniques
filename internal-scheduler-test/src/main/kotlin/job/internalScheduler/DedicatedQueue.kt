package job.internalScheduler

import job.impl.dedicatedQueue.consumer.runConsumer
import job.impl.dedicatedQueue.generator.runGenerator
import job.internalScheduler.services.withExternalActiveMQBroker
import job.metrics.MetricsResult

fun dedicatedQueue(): MetricsResult? {
  val testImplConfig = TestImplConfig
    .getDefault("dedicatedQueue")
  return withExternalActiveMQBroker(testImplConfig.brokerUri, testImplConfig.brokerName) { brokerJmxHost, brokerName ->
    val metricsResult = testImpl(
      testImplConfig,
      { sig, config ->
        runConsumer(sig, config.brokerUri, config.idleTime, config.cacheTime, brokerJmxHost, brokerName)
      },
      { sig, config ->
        runGenerator(
          sig,
          config.brokerUri,
          brokerJmxHost,
          brokerName,
          config.repoCount,
          config.totalJobs,
          config.produceDelay
        )
      }
    )
    println("Dedicated queue metrics: $metricsResult")
    metricsResult
  }
}
