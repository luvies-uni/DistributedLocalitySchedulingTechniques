package job.generator.roundRobin

import job.broker.Producer
import job.data.RepositoryJob

fun main() {
  Producer("tcp://localhost:61616").use { producer ->
    producer.send("jobs/generic", RepositoryJob("repo", "task"))
  }
}
