package job.broker

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

fun shutdownWrapper(fn: (signal: Signal) -> Unit) {
  val signal = Signal()
  val lock = ReentrantLock()

  val shutdownThread = thread(start = false) {
    signal.run = false

    // We attempt to hold the lock so we block until the main thread is done
    lock.withLock { }
  }

  Runtime.getRuntime().addShutdownHook(shutdownThread)

  lock.withLock {
    fn(signal)
  }

  if (signal.run) {
    Runtime.getRuntime().removeShutdownHook(shutdownThread)

    // This allows us to use the signal to control background threads
    signal.run = false
  }
}

data class Signal(var run: Boolean = true)
