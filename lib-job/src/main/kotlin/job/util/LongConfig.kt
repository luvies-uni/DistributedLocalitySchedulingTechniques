package job.util

import java.lang.Thread.sleep

class LongConfig {
  companion object {
    const val dockerActiveMQUri = "tcp://activemq:61616"
    const val dockerRedisUri = "redis"
    const val repoCount = 50
    const val totalJobs = 360
    const val produceDelay: Long = 30_000 // ms
    const val cacheTime: Long = 30 * 60_000 // ms
    const val idleTime: Long = 30_000 // ms

    private const val dedicatedServerIp = "localhost" // The IP of the host running the dedicated ActiveMQ instance
    const val jmxActiveMQUri = "tcp://$dedicatedServerIp:61616"
    const val jmxHostUri = "$dedicatedServerIp:1099"
    const val jmxBrokerName = "localhost"

    @JvmStatic
    fun generatorWait() {
      sleep(18_000)
    }

    @JvmStatic
    fun consumerWait() {
      sleep(14_000)
    }
  }
}
