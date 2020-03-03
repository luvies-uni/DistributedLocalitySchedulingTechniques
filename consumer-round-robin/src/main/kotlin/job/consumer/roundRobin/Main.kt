package job.consumer.roundRobin

import job.broker.Consumer

fun main() {
  Consumer("tcp://localhost:61616").use { consumer ->
    consumer.receive("jobs/generic")
  }
}
