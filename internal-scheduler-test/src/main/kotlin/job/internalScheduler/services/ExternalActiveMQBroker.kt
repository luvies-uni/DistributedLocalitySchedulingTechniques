package job.internalScheduler.services

import org.apache.activemq.broker.BrokerService
import java.lang.Thread.sleep
import java.nio.file.Path

private val brokerSh = Path
  .of(System.getProperty("user.dir"), "broker", "broker.sh")
  .toString()
private const val brokerJmxHost = "localhost:1099"

fun withExternalActiveMQBroker(
  brokerUri: String,
  brokerName: String,
  block: (brokerJmxHost: String, brokerName: String) -> Unit
) {
  val broker = BrokerService()
  broker.brokerName = brokerName
  broker.isUseJmx = true
  broker.isPersistent = false
  broker.addConnector(brokerUri)
  broker.start()

  // Sleep until broker has kicked up
  sleep(1_000)

  try {
    block(brokerJmxHost, brokerName)
  } finally {
    // Let things disconnect naturally first
    sleep(10_000)
    broker.stop()
  }
}
