package job.generator.roundRobin

import job.broker.Producer

fun main() {
  Producer("tcp://localhost:61616").use { producer ->
    producer.send("jobs/generic", "you failed the vibe check 🥺")
  }
}
