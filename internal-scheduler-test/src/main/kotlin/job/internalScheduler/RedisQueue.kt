package job.internalScheduler

import job.impl.redisQueue.consumer.runConsumer
import job.impl.redisQueue.generator.runGenerator
import job.internalScheduler.services.RedisService

fun redisQueue() {
  val redisUri = "localhost"
  val redisService = RedisService()

  redisService.reset()
  val processTime = testImpl(
    "redisQueue",
    { sig, config -> runConsumer(sig, config.idleTime, redisUri, config.processorConfig) },
    { sig, config ->
      runGenerator(sig, redisUri, config.brokerUri, config.repoCount, config.totalJobs, config.produceDelay)
    }
  )
  redisService.down()

  println("Redis queue took ${processTime}ms")
}
