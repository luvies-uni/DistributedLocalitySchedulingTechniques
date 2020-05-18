package job.impl.redisQueue.consumer

import job.broker.JobConsumer
import job.broker.shutdownWrapper
import job.data.Processor
import job.data.ProcessorConfig
import job.util.*
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Response

fun main() {
  shutdownWrapper { sig ->
    runConsumer(
      sig,
      20_000,
      "localhost",
      ProcessorConfig("tcp://localhost:61616", 5000, 1000, 60000)
    )
  }
}

fun runConsumer(sig: Signal, idleTime: Long, redisUri: String, processorConfig: ProcessorConfig) {
  // Create required resources
  JobConsumer(processorConfig.brokerUri).use { consumer ->
    Processor(processorConfig).use { processor ->
      JedisPool(redisUri).use { pool ->
        // Repos that are currently subscribed to
        val currentRepos = mutableSetOf<String>()
        // The time that a job was last processed at
        var lastJobTime = 0L
        val updateJobTime = { lastJobTime = System.currentTimeMillis() }
        // Whether we are currently processing a job
        val jobProc = ProcessingFlag()

        // Main run loop
        val mainLoop = {
          runLogger.info("Fetching active repositories...")
          val repos = pool.resource.use { it.smembers(redisRepoListKey) }
          val currentTime = System.currentTimeMillis()

          // Handle subscribing to new queues
          if (!jobProc.processing && currentTime - lastJobTime > idleTime) {
            runLogger.info("Idle time reached, preparing to subscribe to a new queue...")
            val newRepos = repos.except(currentRepos)

            if (newRepos.isNotEmpty()) {
              runLogger.info("Loading backlog counts for potential queues...")
              // Load the job counts for every new repo
              val jobCounts = pool.resource
                .use {
                  // Pipeline the request to prevent sending multiple synchronous requests
                  it.pipelined().use { p ->
                    val jc: List<Pair<String, Response<String?>>> = newRepos.map { repo ->
                      repo to p.get(repo.toRedisRepoJobCountKey())
                    }
                    p.sync()
                    jc
                  }
                }
                .map { (repo, res) ->
                  repo to (res.get()?.toInt() ?: 0)
                }

              // Choose a new repo to follow weighted based on the number of jobs waiting
              jobCounts.weightedChoose { it.second.toLong() }?.let { (repo, backlogSize) ->
                runLogger.info("Chose repository queue {} (backlog: {})", repo, backlogSize)

                consumer.startJobListen(
                  repo.toRepoQueue(),
                  { job, _ ->
                    jobProc.hold {
                      processor.process(job)
                      pool.resource.use { it.decr(repo.toRedisRepoJobCountKey()) }
                      updateJobTime()
                    }
                  },
                  { !jobProc.processing }
                )

                updateJobTime()
              }
            }
          }

          // Unsubscribe from queues that should no longer exist
          val removedRepos = currentRepos.except(repos)
          if (removedRepos.isNotEmpty()) {
            runLogger.info("Unsubscribing from repository queues ({})", removedRepos)
            for (repo in removedRepos) {
              consumer.stopListen(repo.toRepoQueue())
            }
          }
        }

        // Start up consumer
        mainLoop()
        while (sig.waitForExit(10_000)) {
          mainLoop()
        }
      }
    }
  }
}
