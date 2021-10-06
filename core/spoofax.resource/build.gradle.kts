plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api("org.metaborg:common")
  api("org.metaborg:resource")

  compileOnly("org.checkerframework:checker-qual-android")
}
