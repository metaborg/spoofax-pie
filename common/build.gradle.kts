plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":depconstraints")))

  api("org.metaborg:org.spoofax.terms")

  compileOnly("org.checkerframework:checker-qual-android")
}
