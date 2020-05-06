package job.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val runLogger
  get() = RunLogger.instance.logger

class RunLogger {
  val logger: Logger = LoggerFactory.getLogger(javaClass)

  companion object {
    @JvmStatic
    val instance = RunLogger()
  }
}
