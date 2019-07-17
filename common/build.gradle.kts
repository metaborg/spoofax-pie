plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":depconstraints")))

  api("org.metaborg:resource")

  compileOnly("org.checkerframework:checker-qual-android")
}
