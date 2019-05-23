plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":depconstraints")))

  api(project(":common"))

  api("org.metaborg:statix.solver")

  compileOnly("org.checkerframework:checker-qual-android")
}
