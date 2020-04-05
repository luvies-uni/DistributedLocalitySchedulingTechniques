package job.broker

import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.activemq.ActiveMQSession
import javax.jms.Connection
import javax.jms.Session

internal const val timingStartQueueName = "_timing/start"
internal const val timingCountQueueName = "_timing/count"

open class ActiveMQConnection(brokerUri: String) : AutoCloseable {
  protected val connection: Connection
  internal val session: Session

  init {
    // Create a ConnectionFactory
    val connectionFactory =
      ActiveMQConnectionFactory(brokerUri)

    // Create a Connection
    connection = connectionFactory.createConnection()
    connection.start()

    // Create a Session
    session = connection.createSession(false, ActiveMQSession.INDIVIDUAL_ACKNOWLEDGE)
  }

  override fun close() {
    session.close()
    connection.close()
  }
}
