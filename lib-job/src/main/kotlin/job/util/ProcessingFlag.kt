package job.util

class ProcessingFlag(@Volatile var processing: Boolean = false) {
  fun hold(block: () -> Unit) {
    processing = true
    try {
      block()
    } finally {
      processing = false
    }
  }
}
