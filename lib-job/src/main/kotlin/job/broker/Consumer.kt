package job.broker

import org.slf4j.LoggerFactory
import javax.jms.*

typealias MessageHandler<T> = (data: T, queue: String) -> Unit
/**
 * Converts the contents of a message to the type given.
 *
 * If null is returned, then the JMS message will _not_ be acknowledged.
 */
typealias MessageMapper<T> = (message: String) -> T?

open class Consumer(brokerUri: String) : ActiveMQConn(brokerUri), AutoCloseable, ExceptionListener {
  private val logger = LoggerFactory.getLogger(javaClass)

  private val consumers = mutableMapOf<String, Listener<*>>()

  init {
    connection.exceptionListener = this
  }

  fun <T> receive(queue: String, timeout: Long? = null, msgMapper: MessageMapper<T>): T? {
    return createConsumer(queue).use { consumer ->
      val message: Message? = when (timeout) {
        null -> consumer.receive()
        else -> consumer.receive(timeout)
      }

      handleMessage(queue, message, msgMapper)
    }
  }

  fun <T> startListen(queue: String, msgMapper: MessageMapper<T>): (handler: MessageHandler<T>) -> Boolean {
    return { handler ->
      if (!consumers.containsKey(queue)) {
        consumers[queue] = Listener(queue, handler, msgMapper)
        true
      } else {
        false
      }
    }
  }

  fun stopListen(queue: String): Boolean {
    return consumers[queue]?.let {
      it.close()
      consumers.remove(queue)
      true
    } ?: false
  }

  private fun createConsumer(queue: String): JmsConsumer {
    return JmsConsumer.create(this, queue)
  }

  private fun <T> handleMessage(queue: String, message: Message?, msgMapper: MessageMapper<T>): T? {
    return when (message) {
      null -> {
        logger.debug("Received null message for {}", queue)
        null
      }
      !is TextMessage -> {
        logger.warn("Received non-TextMessage from {}", queue)
        null
      }
      else -> {
        val data = msgMapper(message.text)

        if (data != null) {
          message.acknowledge()
          logger.info("Received {} from {} (acknowledged)", data, queue)
        } else {
          logger.info("Received {} from {} (ignored)", message.text, queue)
        }

        data
      }
    }
  }

  override fun close() {
    for ((_, consumer) in consumers) {
      consumer.close()
    }
    consumers.clear()

    super.close()
  }

  override fun onException(exception: JMSException) {
    logger.error("JMS exception occurred", exception)
  }

  inner class Listener<T>(
    private val queue: String,
    private val handler: MessageHandler<T>,
    private val msgMapper: MessageMapper<T>
  ) : AutoCloseable, MessageListener {
    private var consumer = createConsumer(queue)

    init {
      consumer.messageListener = this
      logger.info("Now listening on $queue")
    }

    override fun onMessage(message: Message?) {
      val data = handleMessage(queue, message, msgMapper)
      if (data != null) {
        handler(data, queue)
      } else {
        // Close the connection to force rejection of the message.
        consumer.close()
        consumer = createConsumer(queue)
        consumer.messageListener = this
      }
    }

    override fun close() {
      consumer.close()
      logger.info("Stopped listening on $queue")
    }
  }
}

internal class JmsConsumer(c: MessageConsumer) : MessageConsumer by c, AutoCloseable {
  companion object {
    @JvmStatic
    fun create(conn: ActiveMQConn, queue: String): JmsConsumer {
      // Create the destination (Topic or Queue)
      val destination = conn.session.createQueue(queue)

      // Create a MessageConsumer from the Session to the Topic or Queue
      return JmsConsumer(conn.session.createConsumer(destination))
    }
  }
}
