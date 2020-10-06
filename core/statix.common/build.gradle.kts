plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api(project(":common"))

  api("org.metaborg:statix.solver")

  implementation(project(":stratego.common"))

  compileOnly("org.checkerframework:checker-qual-android")
}
