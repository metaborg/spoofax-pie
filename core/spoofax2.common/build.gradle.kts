plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  implementation("org.metaborg:common")
  implementation(project(":stratego.common"))

  api("org.metaborg:resource")
  api("org.metaborg:log.api")

  implementation("org.metaborg.devenv:org.spoofax.interpreter.core")
  implementation("com.google.guava:guava:26.0-jre") // Used for AResourcesPrimitive files cache. Synced with Spoofax 2 Guava version.

  compileOnly("org.checkerframework:checker-qual-android")
}
