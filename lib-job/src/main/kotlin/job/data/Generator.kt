package job.data

import java.util.*

class Generator(repoCount: Int) {
  private val repos = Array(repoCount) { UUID.randomUUID().toString() }

  fun nextJob(): RepositoryJob {
    return RepositoryJob(repos.random(), UUID.randomUUID().toString())
  }
}
