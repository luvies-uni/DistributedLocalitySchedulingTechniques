package job.impl.roundRobin.generator

import job.broker.JobProducer
import job.broker.shutdownWrapper
import job.consts.genericJobQueue
import job.data.Generator
import job.util.LongConfig
import job.util.Signal
import java.lang.Thread.sleep

fun main() {
  LongConfig.generatorWait()
  shutdownWrapper { sig ->
    runGenerator(
      sig,
      // Test config
//      "tcp://localhost:61616",
//      10,
//      10,
//      1000
      // Long running config
      LongConfig.dockerActiveMQUri,
      LongConfig.repoCount,
      LongConfig.totalJobs,
      LongConfig.produceDelay
    )

    sig.waitForExit()
  }
}

fun runGenerator(sig: Signal, brokerUri: String, repoCount: Int, totalJobs: Int, produceDelay: Long?) {
  JobProducer(brokerUri).use { producer ->
    val generator = Generator(repoCount)
    producer.startTiming(totalJobs)

    var doneJobs = 0
    while (sig.run && doneJobs < totalJobs) {
      producer.sendJob(genericJobQueue, generator.nextJob())
      doneJobs++

      if (produceDelay != null) {
        // Allow process to close during wait time.
        sleep(produceDelay)
      }
    }
  }
}
