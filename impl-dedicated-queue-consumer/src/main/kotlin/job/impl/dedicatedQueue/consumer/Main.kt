package job.impl.dedicatedQueue.consumer

import job.broker.JobConsumer
import job.broker.jmx.BrokerMetadata
import job.broker.shutdownWrapper
import job.consts.genericJobQueue
import job.data.Processor
import job.metrics.MetricsSender
import job.util.ProcessingFlag
import job.util.Signal
import job.util.toRepoQueue
import job.util.weightedChoose

fun main() {
  shutdownWrapper { sig ->
    runConsumer(
      sig,
      "tcp://localhost:61616",
      60_000,
      5 * 60_000,
      "localhost:1099",
      "localhost"
    )
  }
}

fun runConsumer(
  sig: Signal,
  brokerUri: String,
  idleTime: Long,
  cacheTime: Long,
  brokerJmxHost: String,
  brokerName: String
) {
  MetricsSender(brokerUri).use { metricsSender ->
    JobConsumer(brokerUri, metricsSender).use { consumer ->
      val processor = Processor(cacheTime, metricsSender)
      BrokerMetadata(brokerJmxHost, brokerName).use { metadata ->
        // Queues that are currently subscribed to (excluding the generic queue)
        val currentQueues = mutableSetOf<String>()
        // The time that a job was last processed at
        var lastJobTime = 0L
        val updateJobTime = { lastJobTime = System.currentTimeMillis() }
        // Whether we are currently processing a job
        val jobProc = ProcessingFlag()

        // Starts listening to a given queue
        val startListen = { queue: String ->
          if (currentQueues.add(queue)) {
            consumer.startJobListen(
              queue,
              { job, _ ->
                jobProc.hold {
                  processor.process(job)
                  updateJobTime()
                }
              },
              { !jobProc.processing }
            )
          }
        }

        // Handles unsubscribing from queues when the cache expires
        processor.cacheDropHandler = {
          val queue = it.toRepoQueue()
          consumer.stopListen(queue)
          currentQueues.remove(queue)
        }

        // Start listening to the generic queue
        consumer.startJobListen(
          genericJobQueue,
          { job, _ ->
            jobProc.hold {
              processor.process(job)
              updateJobTime()
            }

            // Since we now have a cache for the repo, start listening to the dedicated queue
            startListen(job.repository.toRepoQueue())
          },
          { !jobProc.processing }
        )

        while (sig.waitForExit(5_000)) {
          // If we have been waiting for too long search to see if any other queues have a backlog
          if (!jobProc.processing && System.currentTimeMillis() - lastJobTime > idleTime) {
            metadata.listQueues()
              .filter { it.backlog > 0 && !currentQueues.contains(it.name) }
              .weightedChoose { it.backlog }
              ?.let {
                startListen(it.name)
                updateJobTime()
              }
          }
        }
      }
    }
  }
}
