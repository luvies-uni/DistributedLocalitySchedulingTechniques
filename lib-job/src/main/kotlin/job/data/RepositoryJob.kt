package job.data

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class RepositoryJob(val repository: String, val task: String) {
  fun stringify(): String {
    return json.stringify(serializer(), this)
  }

  companion object {
    @JvmStatic
    val json = Json(JsonConfiguration.Default)

    @JvmStatic
    fun parse(str: String): RepositoryJob {
      return json.parse(serializer(), str)
    }
  }
}
