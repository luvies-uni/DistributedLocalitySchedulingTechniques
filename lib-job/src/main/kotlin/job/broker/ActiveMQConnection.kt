package job.broker

import org.apache.activemq.ActiveMQConnectionFactory
import javax.jms.Connection
import javax.jms.Session

open class ActiveMQConnection(brokerUri: String) : AutoCloseable {
  protected val connection: Connection
  protected val session: Session

  init {
    // Create a ConnectionFactory
    val connectionFactory =
      ActiveMQConnectionFactory(brokerUri)

    // Create a Connection
    connection = connectionFactory.createConnection()
    connection.start()

    // Create a Session
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
  }

  override fun close() {
    session.close()
    connection.close()
  }
}
