package job.internalScheduler

import job.broker.shutdownWrapper
import job.data.ProcessorConfig
import job.impl.roundRobin.consumer.runConsumer
import job.impl.roundRobin.generator.runGenerator
import job.metrics.Collector
import kotlin.concurrent.thread

fun roundRobin() {
  shutdownWrapper { sig ->
    val brokerUri = "vm://roundRobin?broker.persistent=false"

    var processTime = 0L
    val metricsThread = thread(name = "Metrics Thread") {
      Collector(brokerUri).use {
        it.startJobTiming()
        it.waitForJobTiming(sig)

        val totalJobsProcessTime = it.totalJobsProcessTime
        if (totalJobsProcessTime != null) {
          processTime = totalJobsProcessTime
        }
      }
    }

    println("Started metrics collection thread")

    val processorConfig = ProcessorConfig(brokerUri, 5000, 1000, 60000)
    val consumerThreads = (1..10).map {
      thread(name = "Consumer Thread $it") {
        runConsumer(sig, processorConfig)
      }
    }

    println("Started ${consumerThreads.size} consumer threads")

    runGenerator(sig, brokerUri, 10, 100, 100)

    println("Generated 100 jobs")

    metricsThread.join()

    println("Round robin took ${processTime}ms")
  }
}
