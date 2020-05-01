package job.impl.dedicatedQueue.consumer

import org.apache.activemq.ActiveMQConnection
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.broker.jmx.DestinationViewMBean
import org.apache.activemq.command.ActiveMQQueue
import javax.management.MBeanServerInvocationHandler
import javax.management.ObjectName
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL


fun main() {
//  println(listQueues())
  println(queueSize("jobs/generic"))
}

fun listQueues(): Set<ActiveMQQueue> {
  val connectionFactory = ActiveMQConnectionFactory("tcp://localhost:61616")
  val conn = connectionFactory.createConnection() as ActiveMQConnection
  val ds = conn.destinationSource
  return ds.queues
}

fun queueSize(queue: String): Long {
  val url = JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1616/jmxrmi")

  val environment = mapOf(JMXConnector.CREDENTIALS to arrayOf("admin", "activemq"))

  val jmxc = JMXConnectorFactory.connect(url, environment)
  val connection = jmxc.mBeanServerConnection

  val nameConsumers =
    ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=${queue}")
  val mbView = MBeanServerInvocationHandler.newProxyInstance(
    connection, nameConsumers,
    DestinationViewMBean::class.java, true
  )
  return mbView.queueSize
}
