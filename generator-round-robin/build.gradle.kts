plugins {
  kotlin("jvm") version "1.3.61"
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("junit", "junit", "4.12")

  implementation(project(":lib-job"))
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.includeRuntime = true
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  jar {
    manifest {
      attributes["Main-Class"] = "job.generator.MainKt"
    }
  }
}
