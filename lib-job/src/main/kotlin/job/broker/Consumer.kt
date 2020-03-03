package job.broker

import org.slf4j.LoggerFactory
import javax.jms.*

class Consumer(brokerUri: String) : ActiveMQConnection(brokerUri), AutoCloseable, ExceptionListener {
  private val logger = LoggerFactory.getLogger(this.javaClass)

  private val consumers = mutableMapOf<String, Listener>()

  init {
    connection.exceptionListener = this
  }

  fun receive(queue: String, timeout: Long? = null) {
    val consumer = createConsumer(queue)

    val message = when (timeout) {
      null -> consumer.receive()
      else -> consumer.receive(timeout)
    }

    handleMessage(queue, message)

    consumer.close()
  }

  fun startListen(queue: String): Boolean {
    return if (!consumers.containsKey(queue)) {
      consumers[queue] = Listener(queue)
      true
    } else {
      false
    }
  }

  fun stopListen(queue: String): Boolean {
    val consumer = consumers[queue]
    return if (consumer != null) {
      consumer.close()
      consumers.remove(queue)
      true
    } else {
      false
    }
  }

  private fun handleMessage(queue: String, message: Message?) {
    if (message is TextMessage) {
      val text: String = message.text
      logger.info("Received: $text from $queue")
    } else {
      logger.info("Received: $message from $queue")
    }
  }

  private fun createConsumer(queue: String): MessageConsumer {
    // Create the destination (Topic or Queue)
    val destination = session.createQueue(queue)

    // Create a MessageConsumer from the Session to the Topic or Queue
    return session.createConsumer(destination)
  }

  override fun close() {
    for ((_, consumer) in consumers) {
      consumer.close()
    }
    consumers.clear()

    super.close()
  }

  override fun onException(exception: JMSException?) {
    logger.error("JMS exception occurred", exception)
  }

  inner class Listener(private val queue: String) : AutoCloseable, MessageListener {
    private val consumer: MessageConsumer = createConsumer(queue)

    init {
      consumer.messageListener = this
      logger.info("Now listening on $queue")
    }

    override fun onMessage(message: Message?) {
      handleMessage(queue, message)
    }

    override fun close() {
      consumer.close()
      logger.info("Stopped listening on $queue")
    }
  }
}
