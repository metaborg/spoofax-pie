plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":depconstraints")))

  compileOnly("org.checkerframework:checker-qual-android")
}
