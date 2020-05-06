package job.internalScheduler

import job.broker.shutdownWrapper
import job.data.ProcessorConfig
import job.metrics.Collector
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
): Long {
  var processTime = 0L

  shutdownWrapper { sig ->
    val metricsThread = thread(name = "Metrics Thread") {
      Collector(config.brokerUri).use {
        it.startJobTiming()
        it.waitForJobTiming(sig)

        val totalJobsProcessTime = it.totalJobsProcessTime
        if (totalJobsProcessTime != null) {
          processTime = totalJobsProcessTime
        }
      }
    }

    println("Started metrics collection thread")

    val consumerThreads = (1..10).map {
      thread(name = "Consumer Thread $it") {
        runConsumer(sig, config)
      }
    }

    println("Started ${consumerThreads.size} consumer threads")

    runGenerator(sig, config)

    println("Generated 100 jobs")

    metricsThread.join()
  }

  return processTime
}

data class TestImplConfig(
  // Internal configuration
  val brokerName: String,
  val consumers: Int,

  // Consumer config
  val downloadTime: Long,
  val processTime: Long,
  val cacheTime: Long,
  val idleTime: Long,

  // Generator
  val repoCount: Int,
  val totalJobs: Int,
  val produceDelay: Long?
) {
  val brokerUri = "vm://$brokerName?broker.persistent=false"
  val processorConfig = ProcessorConfig(brokerUri, downloadTime, processTime, cacheTime)

  companion object {
    @JvmStatic
    fun getDefault(brokerName: String) = TestImplConfig(
      brokerName,
      10,
      5000,
      1000,
      60_000,
      10_000,
      10,
      100,
      100
    )
  }
}
