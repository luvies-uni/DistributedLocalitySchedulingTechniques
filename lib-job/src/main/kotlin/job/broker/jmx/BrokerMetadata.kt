package job.broker.jmx

import org.apache.activemq.broker.jmx.BrokerViewMBean
import org.apache.activemq.broker.jmx.DestinationViewMBean
import javax.management.MBeanServerConnection
import javax.management.MBeanServerInvocationHandler
import javax.management.ObjectName
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

fun String.toJmxServiceUri(): String {
  return "service:jmx:rmi:///jndi/rmi://${this}/jmxrmi"
}

class BrokerMetadata(brokerJmxHost: String, private val brokerName: String) : AutoCloseable {
  private val jmxc: JMXConnector
  private val conn: MBeanServerConnection

  init {
    val url = JMXServiceURL(brokerJmxHost.toJmxServiceUri())

    jmxc = JMXConnectorFactory.connect(url)
    conn = jmxc.mBeanServerConnection
  }

  fun listQueues(): List<QueueInfo> {
    val brokerView = MBeanServerInvocationHandler.newProxyInstance(
      conn,
      ObjectName("org.apache.activemq:BrokerName=$brokerName,Type=Broker"),
      BrokerViewMBean::class.java,
      true
    )

    return brokerView.queues.map { queue ->
      val queueView = MBeanServerInvocationHandler.newProxyInstance(
        conn,
        queue,
        DestinationViewMBean::class.java,
        true
      )

      QueueInfo(queueView.name, queueView.consumerCount, queueView.queueSize)
    }
  }

  override fun close() {
    jmxc.close()
  }
}

data class QueueInfo(val name: String, val consumers: Long, val backlog: Long)
