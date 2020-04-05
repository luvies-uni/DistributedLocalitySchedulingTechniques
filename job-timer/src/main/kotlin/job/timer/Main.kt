package job.timer

import job.broker.*
import javax.jms.Message
import javax.jms.TextMessage
import kotlin.system.measureTimeMillis

fun main() {
  shutdownWrapper { sig ->
    ActiveMQConnection("tcp://localhost:61616").use { conn ->
      println("Waiting for starting signal")

      val totalJobs = JmsConsumer.create(conn, timingStartQueueName).use { receive(it, sig) } ?: return@shutdownWrapper
      println("Started timing with $totalJobs jobs")
      var currentJobs = 0

      val jobProcessTime = JmsConsumer.create(conn, timingCountQueueName).use { consumer ->
        measureTimeMillis {
          while (currentJobs < totalJobs) {
            currentJobs += receive(consumer, sig) ?: return@measureTimeMillis
          }
        }
      }

      if (currentJobs < totalJobs) {
        println("Stopped timing after ${jobProcessTime}ms")
      } else {
        println("All jobs completed after ${jobProcessTime}ms")
      }
    }
  }
}

fun receive(consumer: JmsConsumer, signal: Signal): Int? {
  while (signal.run) {
    val message: Message? = consumer.receive(1000)
    if (message != null && message is TextMessage) {
      message.acknowledge()
      return message.text.toInt()
    }
  }

  return null
}
