package job.metrics

import job.broker.shutdownWrapper
import job.util.LongConfig

fun main() {
  val brokerUri: String? = System.getenv("BROKER_URI")

  LongConfig.consumerWait()
  shutdownWrapper { sig ->
    Collector(brokerUri ?: "tcp://localhost:61616").use {
      it.startCollection()
      it.waitForJobProcessTimeCollection(sig)

      println(it.results)
    }

    sig.waitForExit()
  }
}
