plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:common")
  api(project(":jsglr1.common"))
  api(project(":stratego.common"))
  api(project(":constraint.common"))
  api(project(":completions.common"))
  api(project(":tego"))

  compileOnly("org.checkerframework:checker-qual-android")
}
