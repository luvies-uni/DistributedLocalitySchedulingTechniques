package job.impl.redisQueue.consumer

import job.broker.shutdownWrapper

fun main() {
  shutdownWrapper { sig ->
    println("Start")
    sig.waitForExit()
    println("End")
  }
  println("Final")
}
