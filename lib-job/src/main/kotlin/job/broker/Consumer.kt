package job.broker

import job.data.RepositoryJob
import org.slf4j.LoggerFactory
import javax.jms.*

typealias MessageHandler = (job: RepositoryJob, queue: String) -> Unit

class Consumer(brokerUri: String) : ActiveMQConnection(brokerUri), AutoCloseable, ExceptionListener {
  private val logger = LoggerFactory.getLogger(this.javaClass)

  private val consumers = mutableMapOf<String, Listener>()

  init {
    connection.exceptionListener = this
  }

  fun receive(queue: String, timeout: Long? = null): RepositoryJob? {
    val consumer = createConsumer(queue)

    val message = when (timeout) {
      null -> consumer.receive()
      else -> consumer.receive(timeout)
    } as TextMessage

    val job = when (message) {
      !is TextMessage -> null
      else -> RepositoryJob.parse(message.text)
    }

    when (job) {
      null -> logger.warn("Received null message from {}", queue)
      else -> logger.info("Received {} from {}", job, queue)
    }

    consumer.close()

    return job
  }

  fun startListen(queue: String, handler: MessageHandler): Boolean {
    return if (!consumers.containsKey(queue)) {
      consumers[queue] = Listener(queue, handler)
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

  inner class Listener(private val queue: String, private val handler: MessageHandler) : AutoCloseable,
    MessageListener {
    private val consumer: MessageConsumer = createConsumer(queue)

    init {
      consumer.messageListener = this
      logger.info("Now listening on $queue")
    }

    override fun onMessage(message: Message?) {
      when (message) {
        null -> logger.warn("Received null message from {}", queue)
        !is TextMessage -> logger.warn("Received non-TextMessage from {}", queue)
        else -> {
          val job = RepositoryJob.parse(message.text)
          logger.info("Received {} from {}", job, queue)
          handler(job, queue)
        }

      }
    }

    override fun close() {
      consumer.close()
      logger.info("Stopped listening on $queue")
    }
  }
}
