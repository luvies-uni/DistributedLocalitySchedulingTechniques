package job.data

import org.apache.commons.math3.distribution.BetaDistribution
import java.util.*
import kotlin.math.roundToLong

// Use a beta distribution for the download times.
// This has been tweaked so that most download times are close to the
// minimum, as this is how you would expect sizes to be distributed
private val downloadBeta = BetaDistribution(0.7, 3.0)

// Same as above, but for the processing times
private val processBeta = BetaDistribution(1.5, 3.0)

private fun Double.scaleTo(range: TimeRange) = ((this * (range.max - range.min)) + range.min).roundToLong()
private val nilUUID = UUID(0, 0).toString()

private val downloadTime = TimeRange(10 * 1000, 30 * 60 * 1000)
private val downloadTimeShort = TimeRange(5 * 1000, 60 * 1000)
private val processTime = TimeRange(5 * 1000, 60 * 1000)
private val processTimeShort = TimeRange(1 * 1000, 10 * 1000)

var useShortTime = false

private val currentDownloadTime
  get() = if (useShortTime) {
    downloadTimeShort
  } else {
    downloadTime
  }

private val currentProcessTime
  get() = if (useShortTime) {
    processTimeShort
  } else {
    processTime
  }

class Generator(repoCount: Int) {
  private val repos = Array(repoCount) {
    RepositoryJob(
      UUID.randomUUID().toString(),
      nilUUID,
      downloadBeta.sample().scaleTo(currentDownloadTime),
      0
    )
  }

  fun nextJob(): RepositoryJob {
    return repos.random().copy(
      task = UUID.randomUUID().toString(),
      processTime = processBeta.sample().scaleTo(currentProcessTime)
    )
  }
}

private data class TimeRange(val min: Long, val max: Long)
