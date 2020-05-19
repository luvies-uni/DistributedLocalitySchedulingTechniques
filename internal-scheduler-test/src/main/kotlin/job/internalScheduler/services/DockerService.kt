package job.internalScheduler.services

import java.io.File
import java.nio.file.Path

private val basePath = Path
  .of(System.getProperty("user.dir"), "scripts", "docker")
  .toString()

abstract class DockerService(service: String) : AutoCloseable {
  private val _serviceFile = "$service.py"
  private val serviceFile
    get() = Path.of(basePath, _serviceFile).toString()

  fun up() = run("up")

  fun down() = run("down")

  fun reset() = run("reset")

  fun test() = run("test") == 0

  override fun close() {
    down()
  }

  private fun run(cmd: String): Int {
    val proc = ProcessBuilder(serviceFile, cmd)
    proc.directory(File(basePath))
    return proc.start().waitFor()
  }
}
