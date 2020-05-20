package job.internalScheduler

import job.data.useShortTime
import job.metrics.MetricsResult

val repeats = 20

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
    if (firstArg == "full-exec") {
      val secondArg = args.drop(1).firstOrNull()
      if (secondArg != null) {
        val scheduler = schedulers[secondArg]

        if (scheduler != null) {
          fullExec(secondArg, scheduler)
          return
        }
      }
    } else {
      val scheduler = schedulers[firstArg]

      if (scheduler != null) {
        scheduler()
        return
      } else {
        println("Unknown scheduler: $firstArg")
      }
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

fun fullExec(name: String, scheduler: () -> MetricsResult?) {
  val ress = (1..repeats).mapNotNull { scheduler() }

  println(name)
  for (res in ress) {
    println("\t$res")
  }
//  val results = mutableMapOf<String, List<MetricsResult>>()
//  for ((scheduler, block) in schedulers) {
//    println("Testing $scheduler $repeats times")
//
//    results[scheduler] = (1..repeats).mapNotNull { block() }
//  }
//
//  for ((scheduler, ress) in results) {
//    println(scheduler)
//    for (res in ress) {
//      println("\t$res")
//    }
//  }
}
