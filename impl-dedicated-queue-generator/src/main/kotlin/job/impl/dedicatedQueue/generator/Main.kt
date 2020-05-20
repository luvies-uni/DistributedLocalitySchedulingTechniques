package job.impl.dedicatedQueue.generator

import job.broker.JobProducer
import job.broker.jmx.BrokerMetadata
import job.broker.shutdownWrapper
import job.consts.genericJobQueue
import job.data.Generator
import job.util.LongConfig
import job.util.Signal
import job.util.toRepoQueue
import java.lang.Thread.sleep

fun main() {
  shutdownWrapper { sig ->
    runGenerator(
      sig,
      // Test config
//      "tcp://localhost:61616",
//      "localhost:1099",
//      "localhost",
//      10,
//      10,
//      10_000
      // Long running config
      LongConfig.jmxActiveMQUri,
      LongConfig.jmxHostUri,
      LongConfig.jmxBrokerName,
      LongConfig.repoCount,
      LongConfig.totalJobs,
      LongConfig.produceDelay
    )
  }
}

fun runGenerator(
  sig: Signal,
  brokerUri: String,
  brokerJmxHost: String,
  brokerName: String,
  repoCount: Int,
  totalJobs: Int,
  produceDelay: Long?
) {
  JobProducer(brokerUri).use { producer ->
    BrokerMetadata(brokerJmxHost, brokerName).use { metadata ->
      val generator = Generator(repoCount)
      producer.startTiming(totalJobs)

      var doneJobs = 0
      while (sig.run && doneJobs < totalJobs) {
        val job = generator.nextJob()
        val targetQueueName = job.repository.toRepoQueue()

        val targetQueue = metadata.listQueues().firstOrNull { it.name == targetQueueName }
        val sendQueue = if (targetQueue != null && targetQueue.consumers > 0) {
          targetQueue.name
        } else {
          genericJobQueue
        }

        producer.sendJob(sendQueue, job)
        doneJobs++

        if (produceDelay != null) {
          // Allow process to close during wait time.
          sleep(produceDelay)
        }
      }
    }
  }
}
