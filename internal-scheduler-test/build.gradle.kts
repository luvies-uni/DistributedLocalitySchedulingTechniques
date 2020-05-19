plugins {
  application
  kotlin("jvm")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("junit", "junit", "4.12")

  implementation(project(":lib-job"))
  implementation(project(":job-metrics"))
  implementation(project(":impl-dedicated-queue-consumer"))
  implementation(project(":impl-dedicated-queue-generator"))
  implementation(project(":impl-redis-queue-consumer"))
  implementation(project(":impl-redis-queue-generator"))
  implementation(project(":impl-round-robin-consumer"))
  implementation(project(":impl-round-robin-generator"))
}

application {
  mainClassName = "job.internalScheduler.MainKt"
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.includeRuntime = true
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
}
