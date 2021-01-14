plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:common")

  api("org.metaborg.devenv:statix.solver")

  implementation(project(":stratego.common"))

  compileOnly("org.checkerframework:checker-qual-android")
}
