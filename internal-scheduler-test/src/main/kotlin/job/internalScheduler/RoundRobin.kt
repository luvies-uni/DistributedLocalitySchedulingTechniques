package job.internalScheduler

import job.broker.shutdownWrapper
import job.data.ProcessorConfig
import job.impl.roundRobin.consumer.runConsumer
import job.impl.roundRobin.generator.runGenerator
import job.metrics.Timer
import kotlin.concurrent.thread

fun roundRobin() {
  shutdownWrapper { sig ->
    val brokerUri = "vm://roundRobin?broker.persistent=false"

    var processTime = 0L
    val timerThread = thread(name = "Timer Thread") {
      Timer(brokerUri).use { processTime = it.timeJobs(sig) }
    }

    println("Started timer thread")

    val processorConfig = ProcessorConfig(brokerUri, 5000, 1000, 60000)
    val consumerThreads = (1..10).map {
      thread(name = "Consumer Thread $it") {
        runConsumer(sig, processorConfig)
      }
    }

    println("Started ${consumerThreads.size} consumer threads")

    runGenerator(sig, brokerUri, 10, 100, 100)

    println("Generated 100 jobs")

    timerThread.join()

    println("Round robin took ${processTime}ms")
  }
}
