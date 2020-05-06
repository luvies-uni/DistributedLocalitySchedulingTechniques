package job.impl.redisQueue.generator

import job.broker.JobProducer
import job.broker.shutdownWrapper
import job.data.Generator
import job.util.Signal
import job.util.redisRepoListKey
import job.util.toRedisRepoJobCountKey
import job.util.toRepoQueue
import redis.clients.jedis.JedisPool

fun main() {
  shutdownWrapper { sig ->
    runGenerator(
      sig, "localhost", "tcp://localhost:61616",
      10, 10, 1000
    )
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
