package job.internalScheduler.test

import job.broker.Timer
import job.broker.shutdownWrapper
import job.consumer.roundRobin.runConsumer
import job.generator.roundRobin.runGenerator
import org.junit.Test
import kotlin.concurrent.thread

class RoundRobinTest {
  @Test
  fun `time round robin method`() {
    shutdownWrapper { sig ->
      val brokerUri = "vm://roundRobin?broker.persistent=false"

      var processTime = 0L
      val timerThread = thread(name = "Timer Thread") {
        Timer(brokerUri).use { processTime = it.timeJobs(sig) }
      }

      println("Started timer thread")

      val consumerThreads = (1..10).map {
        thread(name = "Consumer Thread $it") {
          runConsumer(sig, brokerUri, 5000, 1000, 60000)
        }
      }

      println("Started ${consumerThreads.size} consumer threads")

      runGenerator(sig, brokerUri, 10, 100, 100)

      println("Generated 100 jobs")

      timerThread.join()

      consumerThreads.forEach { it.interrupt() }

      println("Round robin took ${processTime}ms")
    }
  }
}
