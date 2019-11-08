plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":depconstraints")))

  api(project(":common"))
  api(project(":jsglr1.common"))
  api(project(":stratego.common"))
  api(project(":constraint.common"))

  compileOnly("org.checkerframework:checker-qual-android")
}
