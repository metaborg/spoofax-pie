plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api("org.metaborg:common")
  api(project(":spoofax.core"))
  api("org.metaborg:pie.api")
  compileOnly("org.checkerframework:checker-qual-android")
}
