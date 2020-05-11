plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api(project(":jsglr1.common"))
  api("org.metaborg:pie.api")

  compileOnly("org.checkerframework:checker-qual-android")
}
