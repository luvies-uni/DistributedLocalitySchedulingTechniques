package job.broker

import job.data.RepositoryJob
import job.metrics.MetricsSender

typealias JobHandler = (job: RepositoryJob, queue: String) -> Unit
typealias JobPredicate = (job: RepositoryJob) -> Boolean

private val defaultRepositoryJobMapper = { msg: String ->
  RepositoryJob.parse(msg)
}

private fun repositoryJobMapperFactory(msgPredicate: JobPredicate?): (msg: String) -> RepositoryJob? {
  if (msgPredicate != null) {
    return { msg ->
      val job = defaultRepositoryJobMapper(msg)
      if (msgPredicate(job)) {
        job
      } else {
        null
      }
    }
  } else {
    return defaultRepositoryJobMapper
  }
}

class JobConsumer(brokerUri: String, metricsSender: MetricsSender) : Consumer(brokerUri, metricsSender) {
  fun receiveJob(queue: String, timeout: Long? = null, msgPredicate: JobPredicate? = null): RepositoryJob? {
    return receive(queue, timeout, repositoryJobMapperFactory(msgPredicate))
  }

  fun startJobListen(queue: String, handler: JobHandler, msgPredicate: JobPredicate? = null): Boolean {
    return startListen(queue, repositoryJobMapperFactory(msgPredicate))(handler)
  }
}
