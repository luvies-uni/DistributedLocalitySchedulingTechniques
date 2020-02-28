plugins {
  java
  kotlin("jvm") version "1.3.61"
}

group = "uk.ac.york.cs.ld1042"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_1_8
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
      attributes["Main-Class"] = project.findProperty("mainClass") ?: "NULL"
    }
  }
}
