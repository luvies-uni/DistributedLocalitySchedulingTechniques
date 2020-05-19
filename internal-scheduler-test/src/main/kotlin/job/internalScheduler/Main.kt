package job.internalScheduler

import job.data.useShortTime

val schedulers = mapOf(
  "round-robin" to { roundRobin() },
  "redis-queue" to { redisQueue() },
  "dedicated-queue" to { dedicatedQueue() }
)

fun main(args: Array<String>) {
  // Setup generator for local testing
  useShortTime = true

  val firstArg = args.firstOrNull()

  if (firstArg != null) {
    val scheduler = schedulers[firstArg]

    if (scheduler != null) {
      scheduler()
      return
    } else {
      println("Unknown scheduler: $firstArg")
    }
  }

  println("Internal scheduler test")
  println("\tThis executable will run a given scheduler against the in-memory broker (if possible)")
  println()
  println("Available schedulers:")
  for ((scheduler) in schedulers) {
    println("\t$scheduler")
  }
  println()
  println("Notes:")
  println("\tThe redis-queue scheduler will start an external redis service")
  println("\tThe dedicated-queue scheduler will run the JMX connector on localhost:1099")
}
