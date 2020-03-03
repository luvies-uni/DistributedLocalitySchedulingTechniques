plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("junit", "junit", "4.12")

  api("org.apache.activemq", "activemq-core", "5.7.0")
  implementation("org.slf4j", "slf4j-simple", "1.7.30")
  implementation("org.jetbrains.kotlinx", "kotlinx-serialization-runtime", "0.14.0")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.includeRuntime = false
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
}
