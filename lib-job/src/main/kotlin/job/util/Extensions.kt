package job.util

import job.consts.jobQueueBase

fun <T> Collection<T>.weightedChoose(getWeight: (t: T) -> Long): T? {
  var remainingDistance = Math.random() * this.map(getWeight).sum()

  for (t in this) {
    remainingDistance -= getWeight(t)
    if (remainingDistance < 0) {
      return t
    }
  }

  return null
}

fun String.toRepoQueue(): String {
  return "${jobQueueBase}/$this"
}

fun <T> Set<T>.except(other: Set<T>): Set<T> {
  val result: MutableSet<T> = HashSet(this)
  result.removeAll(other)
  return result
}
