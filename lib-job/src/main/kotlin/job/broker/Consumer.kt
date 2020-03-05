package job.broker

import job.data.RepositoryJob
import org.slf4j.LoggerFactory
import javax.jms.*

typealias MessageHandler = (job: RepositoryJob, queue: String) -> Unit
typealias MessagePredicate = (job: RepositoryJob) -> Boolean

class Consumer(brokerUri: String) : ActiveMQConnection(brokerUri), AutoCloseable, ExceptionListener {
  private val logger = LoggerFactory.getLogger(this.javaClass)

  private val consumers = mutableMapOf<String, Listener>()

  init {
    connection.exceptionListener = this
  }

  fun receive(queue: String, msgPredicate: MessagePredicate? = null, timeout: Long? = null): RepositoryJob? {
    val consumer = createConsumer(queue)

    try {
      val message: Message? = when (timeout) {
        null -> consumer.receive()
        else -> consumer.receive(timeout)
      }

      return handleMessage(queue, message, msgPredicate)
    } finally {
      consumer.close()
    }
  }

  fun startListen(queue: String, handler: MessageHandler, msgPredicate: MessagePredicate? = null): Boolean {
    return if (!consumers.containsKey(queue)) {
      consumers[queue] = Listener(queue, handler, msgPredicate)
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

  private fun createConsumer(queue: String): MessageConsumer {
    // Create the destination (Topic or Queue)
    val destination = session.createQueue(queue)

    // Create a MessageConsumer from the Session to the Topic or Queue
    return session.createConsumer(destination)
  }

  private fun handleMessage(queue: String, message: Message?, msgPredicate: MessagePredicate?): RepositoryJob? {
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
        val job = RepositoryJob.parse(message.text)

        if (msgPredicate?.invoke(job) != false) {
          message.acknowledge()
          logger.info("Received {} from {} (acknowledged)", job, queue)
          job
        } else {
          logger.info("Received {} from {} (ignored)", job, queue)
          null
        }
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

  override fun onException(exception: JMSException?) {
    logger.error("JMS exception occurred", exception)
  }

  inner class Listener(
    private val queue: String,
    private val handler: MessageHandler,
    private val msgPredicate: MessagePredicate?
  ) : AutoCloseable, MessageListener {
    private var consumer: MessageConsumer = createConsumer(queue)

    init {
      consumer.messageListener = this
      logger.info("Now listening on $queue")
    }

    override fun onMessage(message: Message?) {
      val job = handleMessage(queue, message, msgPredicate)
      if (job != null) {
        handler(job, queue)
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
