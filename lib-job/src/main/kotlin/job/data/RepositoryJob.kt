package job.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

@Serializable
data class RepositoryJob(val repository: String, val task: String) {
  fun stringify(): String {
    return json.stringify(serializer(), this)
  }

  companion object {
    @JvmStatic
    val json = Json(JsonConfiguration.Stable)

    @JvmStatic
    fun parse(str: String): RepositoryJob {
      return json.parse(serializer(), str)
    }
  }
}
