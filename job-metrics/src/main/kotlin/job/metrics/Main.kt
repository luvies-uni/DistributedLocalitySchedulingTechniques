package job.metrics

import job.broker.Timer
import job.broker.shutdownWrapper

fun main() {
  shutdownWrapper { sig ->
    Timer("tcp://localhost:61616").use { it.timeJobs(sig) }
  }
}
