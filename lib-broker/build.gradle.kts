plugins {
  kotlin("jvm") version "1.3.61"
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testImplementation("junit", "junit", "4.12")

  api("org.apache.activemq", "activemq-core", "5.7.0")
  implementation("org.slf4j", "slf4j-simple", "1.7.30")
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
