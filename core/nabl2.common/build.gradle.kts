plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:common")

  api("org.metaborg.devenv:nabl2.solver")

  compileOnly("org.checkerframework:checker-qual-android")
}
