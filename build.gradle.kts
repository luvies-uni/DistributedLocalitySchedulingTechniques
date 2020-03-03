plugins {
  kotlin("jvm") version "1.3.61" apply false
  kotlin("plugin.serialization") version "1.3.61" apply false
}

allprojects {
  repositories {
    mavenCentral()
  }
}

subprojects {
  apply(plugin = "java")

  group = "uk.ac.york.cs.ld1042"
  version = "1.0-SNAPSHOT"

  configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
  }
}
