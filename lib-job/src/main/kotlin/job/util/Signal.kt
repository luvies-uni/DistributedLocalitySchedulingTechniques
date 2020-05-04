package job.util

/**
 * A class that provides a signalling mechanism for threads to allow a natural shutdown.
 */
class Signal(@Volatile private var _run: Boolean = true) {
  private val monitor = Object()

  /**
   * Whether the application is current in a run state.
   *
   * If false, then all threads using this signal should stop loops and exit.
   */
  val run: Boolean
    get() = _run

  /**
   * Waits until signalled to exit.
   */
  fun waitForExit() {
    synchronized(monitor) {
      while (_run) {
        monitor.wait()
      }
    }
  }

  /**
   * Waits until signalled to exit, or until the timeout is reached.
   *
   * @return The current run state.
   */
  fun waitForExit(milliseconds: Long): Boolean {
    synchronized(monitor) {
      if (!_run) return false
      monitor.wait(milliseconds)
      return _run
    }
  }

  internal fun exit() {
    synchronized(monitor) {
      _run = false
      monitor.notifyAll()
    }
  }

  internal fun start() {
    _run = true
  }
}
