plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))

  api(project(":common"))

  api("org.metaborg:resource")
  api("org.metaborg.devenv:org.spoofax.jsglr")
  api("org.metaborg.devenv:org.spoofax.terms")

  compileOnly("org.checkerframework:checker-qual-android")
}
