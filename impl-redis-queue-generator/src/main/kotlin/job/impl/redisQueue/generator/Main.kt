package job.impl.redisQueue.generator

import job.broker.JobProducer
import job.broker.shutdownWrapper
import job.data.Generator
import job.util.*
import redis.clients.jedis.JedisPool

fun main() {
  LongConfig.generatorWait()
  shutdownWrapper { sig ->
    runGenerator(
      sig,
      // Test config
//      "localhost",
//      "tcp://localhost:61616",
//      10,
//      10,
//      1000
      // Long running config
      LongConfig.dockerRedisUri,
      LongConfig.dockerActiveMQUri,
      LongConfig.repoCount,
      LongConfig.totalJobs,
      LongConfig.produceDelay
    )

    sig.waitForExit()
  }
}

fun runGenerator(
  sig: Signal,
  redisUri: String,
  brokerUri: String,
  repoCount: Int,
  totalJobs: Int,
  produceDelay: Long?
) {
  JobProducer(brokerUri).use { producer ->
    JedisPool(redisUri).use { pool ->
      val generator = Generator(repoCount)
      producer.startTiming(totalJobs)

      var doneJobs = 0
      while (sig.run && doneJobs < totalJobs) {
        val job = generator.nextJob()
        producer.sendJob(job.repository.toRepoQueue(), job)
        pool.resource.use {
          it.pipelined().use { p ->
            p.sadd(redisRepoListKey, job.repository)
            p.incr(job.repository.toRedisRepoJobCountKey())
            p.sync()
          }
        }
        doneJobs++

        if (produceDelay != null) {
          // Allow process to close during wait time.
          Thread.sleep(produceDelay)
        }
      }
    }
  }
}
