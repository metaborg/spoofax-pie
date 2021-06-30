plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api("org.metaborg:common")
  api(project(":spoofax.core"))
  api("org.metaborg:pie.api")
  api("org.metaborg.devenv:org.spoofax.terms")
  compileOnly("org.checkerframework:checker-qual-android")
}
