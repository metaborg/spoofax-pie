plugins {
  id("org.metaborg.gradle.config.java-library")
}

dependencies {
  api(platform(project(":spoofax.depconstraints")))
  annotationProcessor(platform(project(":spoofax.depconstraints")))
  api("org.metaborg:common")
  api(project(":spoofax.core"))
  api("org.metaborg:pie.api")
  api("org.metaborg.devenv:org.spoofax.terms")
  compileOnly("org.derive4j:derive4j-annotation")
  compileOnly("org.checkerframework:checker-qual-android")
  annotationProcessor("org.derive4j:derive4j")
}
