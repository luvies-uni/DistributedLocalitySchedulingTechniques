package job.impl.redisQueue.consumer

import job.broker.JobConsumer
import job.broker.shutdownWrapper
import job.data.Processor
import job.data.ProcessorConfig
import job.metrics.MetricsSender
import job.util.*
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Response

fun main() {
  shutdownWrapper { sig ->
    runConsumer(
      sig,
      "tcp://localhost:61616",
      20_000,
      "localhost",
      ProcessorConfig(5000, 1000, 60000)
    )
  }
}

fun runConsumer(
  sig: Signal,
  brokerUri: String,
  idleTime: Long,
  redisUri: String,
  processorConfig: ProcessorConfig
) {
  // Create required resources
  MetricsSender(brokerUri).use { metricsSender ->
    JobConsumer(brokerUri, metricsSender).use { consumer ->
      val processor = Processor(processorConfig, metricsSender)
      JedisPool(redisUri).use { pool ->
        // Repos that are currently subscribed to
        val currentRepos = mutableSetOf<String>()
        // The time that a job was last processed at
        var lastJobTime = 0L
        val updateJobTime = { lastJobTime = System.currentTimeMillis() }
        // Whether we are currently processing a job
        val jobProc = ProcessingFlag()

        // Starts listening to a given queue
        val startListen = { repo: String ->
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

          currentRepos.add(repo)
          updateJobTime()
        }

        // Handles unsubscribing from queues when the cache expires
        processor.cacheDropHandler = { repo ->
          consumer.stopListen(repo.toRepoQueue())
          currentRepos.remove(repo)
        }

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

                startListen(repo)
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
