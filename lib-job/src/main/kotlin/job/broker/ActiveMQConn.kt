package job.broker

import org.apache.activemq.ActiveMQConnection
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.ActiveMQSession

internal const val timingStartQueueName = "_timing/start"
internal const val timingCountQueueName = "_timing/count"

open class ActiveMQConn(brokerUri: String) : AutoCloseable {
  protected val connection: ActiveMQConnection
  internal val session: ActiveMQSession

  init {
    // Create a ConnectionFactory
    val connectionFactory =
      ActiveMQConnectionFactory(brokerUri)

    // Create a Connection
    connection = connectionFactory.createConnection() as ActiveMQConnection
    connection.start()

    // Create a Session
    session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE) as ActiveMQSession
  }

  override fun close() {
    session.close()
    connection.close()
  }
}
