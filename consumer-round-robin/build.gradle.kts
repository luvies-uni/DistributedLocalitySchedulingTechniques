plugins {
  kotlin("jvm") version "1.3.61"
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testCompile("junit", "junit", "4.12")
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
      attributes["Main-Class"] = "job.consumer.roundRobin.MainKt"
    }
  }
}
