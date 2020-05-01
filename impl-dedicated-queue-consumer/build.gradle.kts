plugins {
  application
  kotlin("jvm")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("junit", "junit", "4.12")

  implementation(project(":lib-job"))
}

application {
  mainClassName = "job.impl.dedicatedQueue.consumer.MainKt"
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
