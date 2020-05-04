package job.metrics

import job.broker.shutdownWrapper

fun main() {
  shutdownWrapper { sig ->
    Collector("tcp://localhost:61616").use {
      it.startJobTiming()
      it.waitForJobTiming(sig)
    }
  }
}
