package job.internalScheduler

import job.broker.shutdownWrapper
import job.metrics.Collector
import job.metrics.MetricsResult
import job.util.Signal
import kotlin.concurrent.thread

fun testImpl(
  brokerName: String,
  runConsumer: (sig: Signal, config: TestImplConfig) -> Unit,
  runGenerator: (sig: Signal, config: TestImplConfig) -> Unit
) = testImpl(TestImplConfig.getDefault(brokerName), runConsumer, runGenerator)

fun testImpl(
  config: TestImplConfig,
  runConsumer: (sig: Signal, config: TestImplConfig) -> Unit,
  runGenerator: (sig: Signal, config: TestImplConfig) -> Unit
): MetricsResult? {
  var metricsResult: MetricsResult? = null

  shutdownWrapper { sig ->
    val metricsThread = thread(name = "Metrics Thread") {
      Collector(config.brokerUri).use {
        it.startCollection()
        it.waitForJobProcessTimeCollection(sig)

        metricsResult = it.results
      }
    }

    println("Started metrics collection thread")

    val consumerThreads = (1..config.consumers).map {
      thread(name = "Consumer Thread $it") {
        runConsumer(sig, config)
      }
    }

    println("Started ${consumerThreads.size} consumer threads")

    runGenerator(sig, config)

    println("Generated 100 jobs")

    metricsThread.join()
  }

  return metricsResult
}

data class TestImplConfig(
  // Internal configuration
  val brokerName: String,
  val consumers: Int,

  // Consumer config
  val cacheTime: Long,
  val idleTime: Long,

  // Generator
  val repoCount: Int,
  val totalJobs: Int,
  val produceDelay: Long?
) {
  val brokerUri = "vm://$brokerName?broker.persistent=false"

  companion object {
    @JvmStatic
    fun getDefault(brokerName: String) = TestImplConfig(
      brokerName,
      10,
      60_000,
      10_000,
      10,
      100,
      1000
    )
  }
}
