plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api(project(":spoofax.core"))
  api("org.metaborg:log.backend.slf4j")
  api("org.slf4j:slf4j-simple:1.7.30")
  api("com.google.jimfs:jimfs:1.1")
  api("org.metaborg:pie.runtime")

  api("org.junit.jupiter:junit-jupiter-api")

  compileOnly("org.checkerframework:checker-qual-android")
}
