package job.util

const val redisRepoListKey = "repos"

fun String.toRedisRepoJobCountKey(): String {
  return "repo:$this:jobCount"
}
