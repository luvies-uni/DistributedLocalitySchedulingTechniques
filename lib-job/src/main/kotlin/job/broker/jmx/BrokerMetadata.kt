package job.broker.jmx

import org.apache.activemq.broker.jmx.BrokerViewMBean
import org.apache.activemq.broker.jmx.DestinationViewMBean
import javax.management.MBeanServerConnection
import javax.management.MBeanServerInvocationHandler
import javax.management.ObjectName
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

class BrokerMetadata(brokerJmxHost: String, private val brokerName: String) : AutoCloseable {
  private val jmxc: JMXConnector
  private val conn: MBeanServerConnection

  init {
    val url = JMXServiceURL("service:jmx:rmi:///jndi/rmi://${brokerJmxHost}/jmxrmi")

    jmxc = JMXConnectorFactory.connect(url)
    conn = jmxc.mBeanServerConnection
  }

  fun listQueues(): List<QueueInfo> {
    val brokerView = MBeanServerInvocationHandler.newProxyInstance(
      conn,
      ObjectName("org.apache.activemq:type=Broker,brokerName=${brokerName}"),
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
